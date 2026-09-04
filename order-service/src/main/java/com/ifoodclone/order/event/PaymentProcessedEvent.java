package com.ifoodclone.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

// Local copy of payment-service's event shape (same JSON fields) -- there's no shared
// module between services, so the JSON contract itself is what's shared, not a class.
// Consumed from the "payment-events" Kafka topic; see application.yml's
// spring.json.value.default.type for how this bypasses the producer's own type header.
@Data
@NoArgsConstructor
public class PaymentProcessedEvent {
    private Long paymentId;
    private Long orderId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
