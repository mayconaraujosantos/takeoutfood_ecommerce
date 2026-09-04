package com.ifoodclone.delivery.event;

import java.time.LocalDateTime;

import com.ifoodclone.delivery.entity.Delivery;

import lombok.Builder;
import lombok.Data;

// Published to the "delivery-events" Kafka topic on every status change.
// notification-service consumes this to generate customer-facing delivery notifications
// (see its own local copy of this event shape).
@Data
@Builder
public class DeliveryStatusChangedEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
    private Long driverId;
    private Delivery.DeliveryStatus status;
    private LocalDateTime timestamp;

    public static DeliveryStatusChangedEvent from(Delivery delivery) {
        return DeliveryStatusChangedEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .customerId(delivery.getCustomerId())
                .driverId(delivery.getDriverId())
                .status(delivery.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
