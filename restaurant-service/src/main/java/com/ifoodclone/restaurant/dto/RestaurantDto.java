package com.ifoodclone.restaurant.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.restaurant.entity.Restaurant;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public class RestaurantDto {

    private RestaurantDto() {
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Nome é obrigatório")
        private String name;
        private String description;
        @NotBlank(message = "Tipo de cozinha é obrigatório")
        private String cuisineType;
        private String address;
        private String phone;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String phone;
    }

    @Data
    @Builder
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String phone;
        private Long ownerId;
        private Boolean active;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static RestaurantInfo from(Restaurant restaurant) {
            return RestaurantInfo.builder()
                    .id(restaurant.getId())
                    .name(restaurant.getName())
                    .description(restaurant.getDescription())
                    .cuisineType(restaurant.getCuisineType())
                    .address(restaurant.getAddress())
                    .phone(restaurant.getPhone())
                    .ownerId(restaurant.getOwnerId())
                    .active(restaurant.getActive())
                    .createdAt(restaurant.getCreatedAt())
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
