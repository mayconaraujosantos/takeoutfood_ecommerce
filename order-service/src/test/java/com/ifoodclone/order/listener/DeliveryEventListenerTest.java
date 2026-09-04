package com.ifoodclone.order.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.event.DeliveryStatusChangedEvent;
import com.ifoodclone.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Event Listener Tests")
class DeliveryEventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private DeliveryEventListener listener;

    @Test
    @DisplayName("Should move the order to OUT_FOR_DELIVERY when the delivery is picked up")
    void shouldMoveToOutForDelivery() {
        listener = new DeliveryEventListener(orderRepository, kafkaTemplate);
        Order order = Order.builder().id(1L).status(OrderStatus.PREPARING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(1L);
        event.setStatus("PICKED_UP");

        listener.onDeliveryStatusChanged(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("order-events"), any(), any());
    }

    @Test
    @DisplayName("Should move the order to DELIVERED when the delivery is completed")
    void shouldMoveToDelivered() {
        listener = new DeliveryEventListener(orderRepository, kafkaTemplate);
        Order order = Order.builder().id(1L).status(OrderStatus.OUT_FOR_DELIVERY).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(1L);
        event.setStatus("DELIVERED");

        listener.onDeliveryStatusChanged(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Should ignore statuses with no order-facing mapping")
    void shouldIgnoreUnmappedStatus() {
        listener = new DeliveryEventListener(orderRepository, kafkaTemplate);
        Order order = Order.builder().id(1L).status(OrderStatus.PREPARING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(1L);
        event.setStatus("ACCEPTED");

        listener.onDeliveryStatusChanged(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should not resurrect an already-terminal order")
    void shouldNotChangeTerminalOrder() {
        listener = new DeliveryEventListener(orderRepository, kafkaTemplate);
        Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(1L);
        event.setStatus("DELIVERED");

        listener.onDeliveryStatusChanged(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should log and ignore an event for an unknown order")
    void shouldIgnoreUnknownOrder() {
        listener = new DeliveryEventListener(orderRepository, kafkaTemplate);
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setOrderId(99L);
        event.setStatus("DELIVERED");

        listener.onDeliveryStatusChanged(event);

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(any(String.class), any(), any());
    }
}
