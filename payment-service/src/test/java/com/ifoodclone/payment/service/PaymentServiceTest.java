package com.ifoodclone.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, kafkaTemplate);
    }

    @Nested
    @DisplayName("process")
    class ProcessTests {

        @Test
        @DisplayName("Should approve a payment with a positive amount and publish an event")
        void shouldApprovePositiveAmount() {
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

            Payment result = paymentService.process(request);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
            verify(kafkaTemplate).send(eq("payment-events"), eq("1"), any());
        }

        @Test
        @DisplayName("Should reject a payment with a non-positive amount")
        void shouldRejectNonPositiveAmount() {
            PaymentDto.ProcessRequest request = PaymentDto.ProcessRequest.builder()
                    .orderId(1L)
                    .amount(BigDecimal.ZERO)
                    .method(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.process(request);

            assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.REJECTED);
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
