package com.ifoodclone.order.event;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

// Local copy of delivery-service's event shape (same JSON fields) -- there's no shared
// module between services, so the JSON contract itself is what's shared, not a class.
// Consumed from the "delivery-events" Kafka topic; see KafkaConsumerConfig for the
// dedicated container factory this needs (a different default.type than payment-events).
@Data
@NoArgsConstructor
public class DeliveryStatusChangedEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
    private Long driverId;
    private String status;
    private LocalDateTime timestamp;
}
