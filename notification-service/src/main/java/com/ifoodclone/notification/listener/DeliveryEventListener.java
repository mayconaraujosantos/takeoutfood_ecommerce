package com.ifoodclone.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.event.DeliveryStatusChangedEvent;
import com.ifoodclone.notification.service.NotificationService;

@Component
public class DeliveryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventListener.class);

    private final NotificationService notificationService;

    public DeliveryEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service",
            containerFactory = "deliveryKafkaListenerContainerFactory")
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        String title;
        String message;

        switch (event.getStatus()) {
            case "ACCEPTED" -> {
                title = "Entregador a caminho";
                message = "Um entregador aceitou seu pedido #" + event.getOrderId();
            }
            case "PICKED_UP" -> {
                title = "Pedido retirado";
                message = "Seu pedido #" + event.getOrderId() + " foi retirado e está a caminho";
            }
            case "DELIVERED" -> {
                title = "Pedido entregue";
                message = "Seu pedido #" + event.getOrderId() + " foi entregue. Bom apetite!";
            }
            case "CANCELLED" -> {
                title = "Entrega cancelada";
                message = "A entrega do pedido #" + event.getOrderId() + " foi cancelada";
            }
            default -> {
                logger.debug("Ignoring delivery status with no notification mapping: {}", event.getStatus());
                return;
            }
        }

        if (event.getCustomerId() == null) {
            logger.warn("Delivery event for order {} has no customerId, skipping notification", event.getOrderId());
            return;
        }

        notificationService.create(event.getCustomerId(), Notification.NotificationType.DELIVERY, title, message,
                event.getOrderId(), event.getStatus());
    }
}
