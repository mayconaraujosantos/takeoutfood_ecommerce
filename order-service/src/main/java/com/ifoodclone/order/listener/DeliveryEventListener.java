package com.ifoodclone.order.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.event.DeliveryStatusChangedEvent;
import com.ifoodclone.order.event.OrderStatusChangedEvent;
import com.ifoodclone.order.repository.OrderRepository;

// Closes the loop the other way: order-service otherwise never learns that a delivery
// progressed, so a customer polling GET /orders/{id} would see it stuck on PREPARING
// forever. Only PICKED_UP and DELIVERED map to an order status change here -- a driver
// merely being ASSIGNED, or a cancelled delivery, doesn't change what the customer sees
// on their order (the restaurant may just get another driver).
@Component
public class DeliveryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventListener.class);
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventListener(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "delivery-events", groupId = "order-service",
            containerFactory = "deliveryKafkaListenerContainerFactory")
    @Transactional
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            OrderStatus newStatus = switch (event.getStatus()) {
                case "PICKED_UP" -> OrderStatus.OUT_FOR_DELIVERY;
                case "DELIVERED" -> OrderStatus.DELIVERED;
                default -> null;
            };

            if (newStatus == null || order.getStatus() == newStatus
                    || order.getStatus() == OrderStatus.DELIVERED
                    || order.getStatus() == OrderStatus.CANCELLED) {
                return;
            }

            order.setStatus(newStatus);
            orderRepository.save(order);
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getId().toString(), OrderStatusChangedEvent.from(order));
        }, () -> logger.warn("Received delivery event for unknown order {}", event.getOrderId()));
    }
}
