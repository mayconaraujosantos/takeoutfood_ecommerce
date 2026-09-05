package com.ifoodclone.delivery.service;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.entity.Delivery.DeliveryStatus;
import com.ifoodclone.delivery.event.DeliveryStatusChangedEvent;
import com.ifoodclone.delivery.event.OrderStatusChangedEvent;
import com.ifoodclone.delivery.repository.DeliveryRepository;

@Service
@Transactional
public class DeliveryService {

    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryService(DeliveryRepository deliveryRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createFromOrderEvent(OrderStatusChangedEvent event) {
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            return;
        }

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(event.getUserId())
                .restaurantId(event.getRestaurantId())
                .deliveryAddress(event.getDeliveryAddress())
                .status(DeliveryStatus.AWAITING_DRIVER)
                .build();

        delivery = deliveryRepository.save(delivery);
        publishStatusChanged(delivery);
    }

    @Transactional(readOnly = true)
    public List<Delivery> listAvailable() {
        return deliveryRepository.findByStatus(DeliveryStatus.AWAITING_DRIVER);
    }

    public Delivery accept(Long deliveryId, Long driverId) {
        Delivery delivery = getById(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.AWAITING_DRIVER) {
            throw new IllegalStateException(
                    "Entrega já foi aceita por outro entregador ou não está disponível");
        }

        delivery.setDriverId(driverId);
        delivery.setStatus(DeliveryStatus.ACCEPTED);
        delivery = deliveryRepository.save(delivery);
        publishStatusChanged(delivery);

        return delivery;
    }

    public Delivery updateStatus(Long deliveryId, Long driverId, boolean isAdmin, DeliveryStatus newStatus) {
        Delivery delivery = getById(deliveryId);

        boolean isAssignedDriver = delivery.getDriverId() != null && delivery.getDriverId().equals(driverId);
        if (!isAssignedDriver && !isAdmin) {
            throw new SecurityException("Você não tem permissão para alterar esta entrega");
        }

        if (!isValidTransition(delivery.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Transição de status não permitida");
        }

        delivery.setStatus(newStatus);
        delivery = deliveryRepository.save(delivery);
        publishStatusChanged(delivery);

        return delivery;
    }

    @Transactional(readOnly = true)
    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
    }

    @Transactional(readOnly = true)
    public Delivery getByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada para este pedido"));
    }

    @Transactional(readOnly = true)
    public List<Delivery> myDeliveries(Long driverId) {
        return deliveryRepository.findByDriverId(driverId);
    }

    private boolean isValidTransition(DeliveryStatus current, DeliveryStatus target) {
        return switch (current) {
            case ACCEPTED -> target == DeliveryStatus.PICKED_UP || target == DeliveryStatus.CANCELLED;
            case PICKED_UP -> target == DeliveryStatus.DELIVERED;
            case AWAITING_DRIVER -> target == DeliveryStatus.CANCELLED;
            default -> false;
        };
    }

    private void publishStatusChanged(Delivery delivery) {
        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, delivery.getId().toString(),
                DeliveryStatusChangedEvent.from(delivery));
    }
}
