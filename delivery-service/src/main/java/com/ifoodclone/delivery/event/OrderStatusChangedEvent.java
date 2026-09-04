package com.ifoodclone.delivery.event;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

// Local copy of order-service's event shape (same JSON fields) -- there's no shared
// module between services, so the JSON contract itself is what's shared, not a class.
// Consumed from the "order-events" Kafka topic; see application.yml's
// spring.json.value.default.type for how this bypasses the producer's own type header.
@Data
@NoArgsConstructor
public class OrderStatusChangedEvent {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}
