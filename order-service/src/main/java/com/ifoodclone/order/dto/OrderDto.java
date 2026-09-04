package com.ifoodclone.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.entity.OrderItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrderDto {

    private OrderDto() {
    }

    // @NoArgsConstructor on every request DTO here: Jackson treats a Lombok-generated
    // single-arg constructor (what @Builder alone produces for a one-field class) as an
    // ambiguous delegating creator and refuses to deserialize a JSON object into it
    // ("no delegate- or property-based Creator"). An explicit no-args constructor +
    // the @Data setters sidesteps that regardless of how many fields a DTO has.
    @Data
    @Builder
    @NoArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Restaurante é obrigatório")
        private Long restaurantId;

        public CreateRequest(Long restaurantId) {
            this.restaurantId = restaurantId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class AddItemRequest {
        @NotNull(message = "Item de cardápio é obrigatório")
        private Long menuItemId;
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        private Integer quantity;
        private String notes;

        public AddItemRequest(Long menuItemId, Integer quantity, String notes) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
            this.notes = notes;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class UpdateItemRequest {
        @Positive(message = "Quantidade deve ser positiva")
        private Integer quantity;
        private String notes;

        public UpdateItemRequest(Integer quantity, String notes) {
            this.quantity = quantity;
            this.notes = notes;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class CheckoutRequest {
        @NotBlank(message = "Endereço de entrega é obrigatório")
        private String deliveryAddress;
        @NotNull(message = "Método de pagamento é obrigatório")
        private Order.PaymentMethod paymentMethod;

        public CheckoutRequest(String deliveryAddress, Order.PaymentMethod paymentMethod) {
            this.deliveryAddress = deliveryAddress;
            this.paymentMethod = paymentMethod;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "Status é obrigatório")
        private Order.OrderStatus status;

        public StatusUpdateRequest(Order.OrderStatus status) {
            this.status = status;
        }
    }

    @Data
    @Builder
    public static class OrderItemInfo {
        private Long id;
        private Long menuItemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String notes;

        public static OrderItemInfo from(OrderItem item) {
            return OrderItemInfo.builder()
                    .id(item.getId())
                    .menuItemId(item.getMenuItemId())
                    .itemName(item.getItemName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .notes(item.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    public static class OrderInfo {
        private Long id;
        private Long userId;
        private Long restaurantId;
        private Order.OrderStatus status;
        private String deliveryAddress;
        private Order.PaymentMethod paymentMethod;
        private BigDecimal totalAmount;
        private List<OrderItemInfo> items;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static OrderInfo from(Order order) {
            return OrderInfo.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .restaurantId(order.getRestaurantId())
                    .status(order.getStatus())
                    .deliveryAddress(order.getDeliveryAddress())
                    .paymentMethod(order.getPaymentMethod())
                    .totalAmount(order.getTotalAmount())
                    .items(order.getItems().stream().map(OrderItemInfo::from).toList())
                    .createdAt(order.getCreatedAt())
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
