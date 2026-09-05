package com.ifoodclone.delivery.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ifoodclone.delivery.event.OrderStatusChangedEvent;
import com.ifoodclone.delivery.service.DeliveryService;

@Component
public class OrderEventListener {

    private final DeliveryService deliveryService;

    public OrderEventListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // PREPARING is when the restaurant starts cooking -- the right moment to already
    // line up delivery logistics. Other statuses are ignored.
    @KafkaListener(topics = "order-events", groupId = "delivery-service")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if ("PREPARING".equals(event.getStatus())) {
            deliveryService.createFromOrderEvent(event);
        }
    }
}
