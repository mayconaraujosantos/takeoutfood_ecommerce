package com.ifoodclone.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.entity.Delivery.DeliveryStatus;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByDriverId(Long driverId);
}
