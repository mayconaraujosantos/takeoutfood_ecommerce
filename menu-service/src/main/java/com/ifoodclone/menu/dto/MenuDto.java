package com.ifoodclone.menu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.menu.entity.MenuItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

public class MenuDto {

    private MenuDto() {
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotNull(message = "Restaurante é obrigatório")
        private Long restaurantId;
        @NotBlank(message = "Nome é obrigatório")
        private String name;
        private String description;
        @NotNull(message = "Preço é obrigatório")
        @Positive(message = "Preço deve ser positivo")
        private BigDecimal price;
        private String category;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Boolean available;
    }

    @Data
    @Builder
    public static class MenuItemInfo {
        private Long id;
        private Long restaurantId;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Boolean available;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static MenuItemInfo from(MenuItem item) {
            return MenuItemInfo.builder()
                    .id(item.getId())
                    .restaurantId(item.getRestaurantId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .price(item.getPrice())
                    .category(item.getCategory())
                    .available(item.getAvailable())
                    .createdAt(item.getCreatedAt())
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
