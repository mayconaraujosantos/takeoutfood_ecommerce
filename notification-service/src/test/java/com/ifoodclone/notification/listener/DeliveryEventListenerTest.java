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
import com.ifoodclone.notification.event.DeliveryStatusChangedEvent;
import com.ifoodclone.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Event Listener Tests")
class DeliveryEventListenerTest {

    @Mock
    private NotificationService notificationService;

    private DeliveryEventListener listener;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        listener = new DeliveryEventListener(notificationService);
    }

    @Test
    @DisplayName("Should create a DELIVERY notification when status is DELIVERED")
    void shouldNotifyOnDelivered() {
        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setDeliveryId(1L);
        event.setOrderId(10L);
        event.setCustomerId(1L);
        event.setDriverId(2L);
        event.setStatus("DELIVERED");
        event.setTimestamp(LocalDateTime.now());

        listener.onDeliveryStatusChanged(event);

        verify(notificationService).create(eq(1L), eq(Notification.NotificationType.DELIVERY),
                eq("Pedido entregue"), org.mockito.ArgumentMatchers.anyString(), eq(10L), eq("DELIVERED"));
    }

    @Test
    @DisplayName("Should skip notification when customerId is missing")
    void shouldSkipWhenNoCustomerId() {
        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(10L);
        event.setStatus("DELIVERED");

        listener.onDeliveryStatusChanged(event);

        verify(notificationService, never()).create(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Should ignore AWAITING_DRIVER status")
    void shouldIgnoreAwaitingDriver() {
        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(10L);
        event.setCustomerId(1L);
        event.setStatus("AWAITING_DRIVER");

        listener.onDeliveryStatusChanged(event);

        verify(notificationService, never()).create(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }
}
