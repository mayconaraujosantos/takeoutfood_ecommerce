package com.ifoodclone.delivery.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.delivery.config.GatewayUserContext.UserContext;
import com.ifoodclone.delivery.dto.DeliveryDto;
import com.ifoodclone.delivery.dto.DeliveryDto.ApiResponse;
import com.ifoodclone.delivery.dto.DeliveryDto.DeliveryInfo;
import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.service.DeliveryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deliveries")
@Tag(name = "Deliveries", description = "Logística e rastreamento de entregas")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DeliveryInfo>>> listAvailable() {
        if (!UserContext.isDeliveryDriver() && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas entregadores podem ver entregas disponíveis"));
        }

        List<DeliveryInfo> deliveries = deliveryService.listAvailable().stream()
                .map(DeliveryInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<DeliveryInfo>> accept(@PathVariable Long id) {
        if (!UserContext.isDeliveryDriver()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas entregadores podem aceitar entregas"));
        }

        try {
            Delivery delivery = deliveryService.accept(id, UserContext.getUserId());
            return ResponseEntity.ok(ApiResponse.success("Entrega aceita com sucesso", DeliveryInfo.from(delivery)));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryInfo>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody DeliveryDto.StatusUpdateRequest request) {
        try {
            Delivery delivery = deliveryService.updateStatus(id, UserContext.getUserId(), UserContext.isAdmin(),
                    request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Status atualizado com sucesso", DeliveryInfo.from(delivery)));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryInfo>> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(DeliveryInfo.from(deliveryService.getById(id))));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryInfo>> getByOrderId(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(DeliveryInfo.from(deliveryService.getByOrderId(orderId))));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DeliveryInfo>>> myDeliveries() {
        List<DeliveryInfo> deliveries = deliveryService.myDeliveries(UserContext.getUserId()).stream()
                .map(DeliveryInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
}
