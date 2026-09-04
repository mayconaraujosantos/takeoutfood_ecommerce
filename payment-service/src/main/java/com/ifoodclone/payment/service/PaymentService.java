package com.ifoodclone.payment.service;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Payment process(PaymentDto.ProcessRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(decideStatus(request.getAmount()))
                .build();

        payment = paymentRepository.save(payment);

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, payment.getOrderId().toString(),
                PaymentProcessedEvent.from(payment));

        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado para este pedido"));
    }

    // No real payment gateway integration -- this is a simulated processor for the
    // demo flow. Rejects non-positive amounts (already guarded by @Positive on the
    // request DTO, kept here too since this is the actual business rule), approves
    // everything else deterministically.
    private Payment.PaymentStatus decideStatus(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return Payment.PaymentStatus.REJECTED;
        }
        return Payment.PaymentStatus.APPROVED;
    }
}
