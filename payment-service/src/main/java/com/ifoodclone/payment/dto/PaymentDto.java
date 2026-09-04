package com.ifoodclone.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.payment.entity.Payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

public class PaymentDto {

    private PaymentDto() {
    }

    @Data
    @Builder
    public static class ProcessRequest {
        @NotNull(message = "Pedido é obrigatório")
        private Long orderId;
        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        private BigDecimal amount;
        @NotNull(message = "Método de pagamento é obrigatório")
        private Payment.PaymentMethod method;
    }

    @Data
    @Builder
    public static class PaymentInfo {
        private Long id;
        private Long orderId;
        private BigDecimal amount;
        private Payment.PaymentMethod method;
        private Payment.PaymentStatus status;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static PaymentInfo from(Payment payment) {
            return PaymentInfo.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .amount(payment.getAmount())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .createdAt(payment.getCreatedAt())
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
