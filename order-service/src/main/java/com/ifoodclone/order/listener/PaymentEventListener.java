package com.ifoodclone.order.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.event.OrderStatusChangedEvent;
import com.ifoodclone.order.event.PaymentProcessedEvent;
import com.ifoodclone.order.repository.OrderRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

// Consumes "payment-events" as a consistency backstop -- checkout() already updates
// the order's status synchronously from the same call that triggers this event, so in
// the normal path this listener just re-confirms a status the order already has. It
// matters when the synchronous checkout call itself failed/timed out after payment-service
// had already committed the charge: this is what still moves the order out of
// PENDING_PAYMENT in that case.
@Component
public class PaymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventListener(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    @Transactional
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return;
            }

            OrderStatus newStatus = "APPROVED".equals(event.getStatus())
                    ? OrderStatus.CONFIRMED
                    : OrderStatus.PAYMENT_FAILED;

            order.setStatus(newStatus);
            orderRepository.save(order);
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getId().toString(), OrderStatusChangedEvent.from(order));
        }, () -> logger.warn("Received payment event for unknown order {}", event.getOrderId()));
    }
}
