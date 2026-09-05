package com.ifoodclone.delivery.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.entity.Delivery.DeliveryStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DeliveryDto {

    private DeliveryDto() {
    }

    // @Data + @Builder alone suppresses the no-args constructor Jackson needs to
    // deserialize a @RequestBody (Lombok only auto-generates one when no other
    // constructor exists, and @Builder's internal all-args constructor counts as one).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "Status é obrigatório")
        private DeliveryStatus status;
    }

    @Data
    @Builder
    public static class DeliveryInfo {
        private Long id;
        private Long orderId;
        private Long customerId;
        private Long restaurantId;
        private String deliveryAddress;
        private Long driverId;
        private DeliveryStatus status;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static DeliveryInfo from(Delivery delivery) {
            return DeliveryInfo.builder()
                    .id(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .customerId(delivery.getCustomerId())
                    .restaurantId(delivery.getRestaurantId())
                    .deliveryAddress(delivery.getDeliveryAddress())
                    .driverId(delivery.getDriverId())
                    .status(delivery.getStatus())
                    .createdAt(delivery.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String error;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> error(String error) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .error(error)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> error(String message, String error) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .error(error)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
