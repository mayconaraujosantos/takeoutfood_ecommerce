package com.ifoodclone.order.event;

import java.time.LocalDateTime;

import com.ifoodclone.order.entity.Order;

import lombok.Builder;
import lombok.Data;

// Published to the "order-events" Kafka topic on every status change. Nothing
// consumes this yet -- notification-service (out of scope for now, still an empty
// skeleton) is the intended future subscriber for customer-facing status updates.
@Data
@Builder
public class OrderStatusChangedEvent {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private Order.OrderStatus status;
    // Added so delivery-service can create a Delivery with a real destination -- it has
    // no other way to learn this (no synchronous, authenticated call is available from a
    // Kafka listener). Consumers that don't care (notification-service) just ignore it.
    private String deliveryAddress;
    private LocalDateTime timestamp;

    public static OrderStatusChangedEvent from(Order order) {
        return OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
