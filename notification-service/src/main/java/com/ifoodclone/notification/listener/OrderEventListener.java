package com.ifoodclone.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.event.OrderStatusChangedEvent;
import com.ifoodclone.notification.service.NotificationService;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service",
            containerFactory = "orderKafkaListenerContainerFactory")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String title;
        String message;

        switch (event.getStatus()) {
            case "PENDING_PAYMENT" -> {
                title = "Pagamento em processamento";
                message = "Estamos processando o pagamento do seu pedido #" + event.getOrderId();
            }
            case "CONFIRMED" -> {
                title = "Pedido confirmado!";
                message = "Pagamento aprovado, seu pedido #" + event.getOrderId() + " foi confirmado";
            }
            case "PAYMENT_FAILED" -> {
                title = "Pagamento recusado";
                message = "Não conseguimos processar o pagamento do pedido #" + event.getOrderId();
            }
            case "PREPARING" -> {
                title = "Pedido em preparo";
                message = "Seu pedido #" + event.getOrderId() + " está sendo preparado";
            }
            case "CANCELLED" -> {
                title = "Pedido cancelado";
                message = "Seu pedido #" + event.getOrderId() + " foi cancelado";
            }
            default -> {
                logger.debug("Ignoring order status with no notification mapping: {}", event.getStatus());
                return;
            }
        }

        notificationService.create(event.getUserId(), Notification.NotificationType.ORDER, title, message,
                event.getOrderId(), event.getStatus());
    }
}
