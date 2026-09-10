package com.ifoodclone.payment.service;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.payment.client.PaymentGatewayClient;
import com.ifoodclone.payment.dto.PaymentDto;
import com.ifoodclone.payment.entity.Payment;
import com.ifoodclone.payment.event.PaymentProcessedEvent;
import com.ifoodclone.payment.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentGatewayClient gatewayClient;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate,
            PaymentGatewayClient gatewayClient) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.gatewayClient = gatewayClient;
    }

    // Charges through the Node-RED gateway simulator, which answers PENDING immediately and
    // resolves APPROVED/REJECTED later via POST /api/v1/payments/webhook -- see resolve().
    // A non-positive amount is rejected synchronously here since that's a request validation
    // error, not something the gateway needs to decide; so is a gateway the client can't
    // even reach, since no webhook will ever arrive for that charge.
    public Payment process(PaymentDto.ProcessRequest request) {
        if (!isPositive(request.getAmount())) {
            return reject(request);
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build());

        boolean accepted = gatewayClient.charge(payment.getOrderId(), payment.getAmount(), payment.getMethod().name());
        if (!accepted) {
            payment.setStatus(Payment.PaymentStatus.REJECTED);
            payment = paymentRepository.save(payment);
            publishProcessed(payment);
        }

        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado para este pedido"));
    }

    // Called by the gateway's webhook once it resolves the charge. Ignores an update for a
    // payment that isn't PENDING anymore -- a duplicate or late webhook delivery shouldn't
    // resurrect or flip an already-decided payment.
    public Payment resolve(Long orderId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado para este pedido"));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            return payment;
        }

        payment.setStatus(status);
        payment = paymentRepository.save(payment);
        publishProcessed(payment);

        return payment;
    }

    private Payment reject(PaymentDto.ProcessRequest request) {
        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(Payment.PaymentStatus.REJECTED)
                .build());
        publishProcessed(payment);
        return payment;
    }

    private boolean isPositive(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    private void publishProcessed(Payment payment) {
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, payment.getOrderId().toString(), PaymentProcessedEvent.from(payment));
    }
}
