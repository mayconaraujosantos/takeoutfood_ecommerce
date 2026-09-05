package com.ifoodclone.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.entity.Delivery.DeliveryStatus;
import com.ifoodclone.delivery.event.OrderStatusChangedEvent;
import com.ifoodclone.delivery.repository.DeliveryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Service Tests")
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository, kafkaTemplate);
    }

    @Nested
    @DisplayName("createFromOrderEvent")
    class CreateFromOrderEventTests {

        @Test
        @DisplayName("Should create a delivery when none exists for the order")
        void shouldCreateDelivery() {
            OrderStatusChangedEvent event = new OrderStatusChangedEvent();
            event.setOrderId(1L);
            event.setUserId(42L);
            event.setRestaurantId(7L);
            event.setStatus("PREPARING");
            event.setDeliveryAddress("Rua Teste, 123");

            when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> {
                Delivery d = inv.getArgument(0);
                d.setId(100L);
                return d;
            });

            deliveryService.createFromOrderEvent(event);

            org.mockito.ArgumentCaptor<Delivery> captor = org.mockito.ArgumentCaptor.forClass(Delivery.class);
            verify(deliveryRepository).save(captor.capture());
            assertThat(captor.getValue().getDeliveryAddress()).isEqualTo("Rua Teste, 123");
            verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("delivery-events"), any(), any());
        }

        @Test
        @DisplayName("Should be idempotent when a delivery already exists for the order")
        void shouldBeIdempotent() {
            OrderStatusChangedEvent event = new OrderStatusChangedEvent();
            event.setOrderId(1L);

            when(deliveryRepository.findByOrderId(1L))
                    .thenReturn(Optional.of(Delivery.builder().id(1L).orderId(1L).build()));

            deliveryService.createFromOrderEvent(event);

            verify(deliveryRepository, never()).save(any(Delivery.class));
        }
    }

    @Nested
    @DisplayName("accept")
    class AcceptTests {

        @Test
        @DisplayName("Should assign the driver and mark as accepted")
        void shouldAcceptDelivery() {
            Delivery delivery = Delivery.builder().id(1L).status(DeliveryStatus.AWAITING_DRIVER).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.accept(1L, 55L);

            assertThat(result.getDriverId()).isEqualTo(55L);
            assertThat(result.getStatus()).isEqualTo(DeliveryStatus.ACCEPTED);
        }

        @Test
        @DisplayName("Should reject accepting a delivery that's not awaiting a driver")
        void shouldRejectAcceptingUnavailableDelivery() {
            Delivery delivery = Delivery.builder().id(1L).driverId(10L).status(DeliveryStatus.ACCEPTED).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThatThrownBy(() -> deliveryService.accept(1L, 55L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should allow the assigned driver to move ACCEPTED to PICKED_UP")
        void shouldAllowAssignedDriverToProgress() {
            Delivery delivery = Delivery.builder().id(1L).driverId(55L).status(DeliveryStatus.ACCEPTED).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus(1L, 55L, false, DeliveryStatus.PICKED_UP);

            assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
        }

        @Test
        @DisplayName("Should reject a non-assigned, non-admin driver")
        void shouldRejectNonAssignedDriver() {
            Delivery delivery = Delivery.builder().id(1L).driverId(55L).status(DeliveryStatus.ACCEPTED).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThatThrownBy(() -> deliveryService.updateStatus(1L, 999L, false, DeliveryStatus.PICKED_UP))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Should reject an invalid status transition")
        void shouldRejectInvalidTransition() {
            Delivery delivery = Delivery.builder().id(1L).driverId(55L).status(DeliveryStatus.ACCEPTED).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThatThrownBy(() -> deliveryService.updateStatus(1L, 55L, false, DeliveryStatus.DELIVERED))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow an admin to cancel regardless of assignment")
        void shouldAllowAdminToCancel() {
            Delivery delivery = Delivery.builder().id(1L).status(DeliveryStatus.AWAITING_DRIVER).build();
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus(1L, 999L, true, DeliveryStatus.CANCELLED);

            assertThat(result.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
        }
    }
}
