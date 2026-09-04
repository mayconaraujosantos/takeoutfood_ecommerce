package com.ifoodclone.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ifoodclone.payment.entity.Payment;

import lombok.Builder;
import lombok.Data;

// Published to the "payment-events" Kafka topic after every payment attempt. order-service
// consumes this to move an Order out of PENDING_PAYMENT without needing another synchronous
// call back -- satisfies the backlog's "event-driven communication for payment status" story.
@Data
@Builder
public class PaymentProcessedEvent {
    private Long paymentId;
    private Long orderId;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public static PaymentProcessedEvent from(Payment payment) {
        return PaymentProcessedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
