package com.ifoodclone.notification.listener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.event.OrderStatusChangedEvent;
import com.ifoodclone.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Event Listener Tests")
class OrderEventListenerTest {

    @Mock
    private NotificationService notificationService;

    private OrderEventListener listener;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        listener = new OrderEventListener(notificationService);
    }

    @Test
    @DisplayName("Should create an ORDER notification when status is CONFIRMED")
    void shouldNotifyOnConfirmed() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent();
        event.setOrderId(10L);
        event.setUserId(1L);
        event.setRestaurantId(5L);
        event.setStatus("CONFIRMED");
        event.setTimestamp(LocalDateTime.now());

        listener.onOrderStatusChanged(event);

        verify(notificationService).create(eq(1L), eq(Notification.NotificationType.ORDER),
                eq("Pedido confirmado!"), org.mockito.ArgumentMatchers.anyString(), eq(10L), eq("CONFIRMED"));
    }

    @Test
    @DisplayName("Should ignore CART status")
    void shouldIgnoreCartStatus() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent();
        event.setOrderId(10L);
        event.setUserId(1L);
        event.setStatus("CART");

        listener.onOrderStatusChanged(event);

        verify(notificationService, never()).create(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }
}
