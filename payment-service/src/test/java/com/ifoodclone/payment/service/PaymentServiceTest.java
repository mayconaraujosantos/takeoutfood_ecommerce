package com.ifoodclone.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.ifoodclone.payment.client.PaymentGatewayClient;
import com.ifoodclone.payment.dto.PaymentDto;
import com.ifoodclone.payment.entity.Payment;
import com.ifoodclone.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private PaymentGatewayClient gatewayClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, kafkaTemplate, gatewayClient);
    }

    @Nested
    @DisplayName("process")
    class ProcessTests {

        @Test
        @DisplayName("Should mark a positive amount as pending and hand the charge to the gateway")
        void shouldSendPositiveAmountToGateway() {
            PaymentDto.ProcessRequest request = PaymentDto.ProcessRequest.builder()
                    .orderId(1L)
                    .amount(new BigDecimal("59.90"))
                    .method(Payment.PaymentMethod.PIX)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(100L);
                return p;
            });
            when(gatewayClient.charge(1L, new BigDecimal("59.90"), "PIX")).thenReturn(true);

            Payment result = paymentService.process(request);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
            verify(gatewayClient).charge(1L, new BigDecimal("59.90"), "PIX");
        }

        @Test
        @DisplayName("Should reject immediately when the gateway can't be reached")
        void shouldRejectWhenGatewayUnreachable() {
            PaymentDto.ProcessRequest request = PaymentDto.ProcessRequest.builder()
                    .orderId(1L)
                    .amount(new BigDecimal("59.90"))
                    .method(Payment.PaymentMethod.PIX)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(gatewayClient.charge(any(), any(), any())).thenReturn(false);

            Payment result = paymentService.process(request);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.REJECTED);
            verify(kafkaTemplate).send(eq("payment-events"), eq("1"), any());
        }

        @Test
        @DisplayName("Should reject a payment with a non-positive amount without calling the gateway")
        void shouldRejectNonPositiveAmount() {
            PaymentDto.ProcessRequest request = PaymentDto.ProcessRequest.builder()
                    .orderId(1L)
                    .amount(BigDecimal.ZERO)
                    .method(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.process(request);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.REJECTED);
            verify(gatewayClient, never()).charge(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("resolve")
    class ResolveTests {

        @Test
        @DisplayName("Should move a pending payment to the webhook's status and publish an event")
        void shouldResolvePendingPayment() {
            Payment pending = Payment.builder().id(1L).orderId(5L).amount(new BigDecimal("10.00"))
                    .method(Payment.PaymentMethod.PIX).status(Payment.PaymentStatus.PENDING).build();
            when(paymentRepository.findByOrderId(5L)).thenReturn(Optional.of(pending));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.resolve(5L, Payment.PaymentStatus.APPROVED);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
            verify(kafkaTemplate).send(eq("payment-events"), eq("5"), any());
        }

        @Test
        @DisplayName("Should ignore a webhook for a payment that's no longer pending")
        void shouldIgnoreNonPendingPayment() {
            Payment approved = Payment.builder().id(1L).orderId(5L).amount(new BigDecimal("10.00"))
                    .method(Payment.PaymentMethod.PIX).status(Payment.PaymentStatus.APPROVED).build();
            when(paymentRepository.findByOrderId(5L)).thenReturn(Optional.of(approved));

            Payment result = paymentService.resolve(5L, Payment.PaymentStatus.REJECTED);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
            verify(paymentRepository, never()).save(any());
            verify(kafkaTemplate, never()).send(any(String.class), any(), any());
        }
    }

    @Nested
    @DisplayName("getByOrderId")
    class GetByOrderIdTests {

        @Test
        @DisplayName("Should throw when no payment exists for the order")
        void shouldThrowWhenMissing() {
            when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getByOrderId(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }
}
