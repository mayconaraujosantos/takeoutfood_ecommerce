package com.ifoodclone.review.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.review.entity.Review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReviewDto {

    private ReviewDto() {
    }

    // @Data + @Builder alone suppresses the no-args constructor Jackson needs to
    // deserialize a @RequestBody (Lombok only auto-generates one when no other
    // constructor exists, and @Builder's internal all-args constructor counts as one).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Restaurante é obrigatório")
        private Long restaurantId;

        private Long orderId;

        @NotNull(message = "Avaliação é obrigatória")
        @Min(value = 1, message = "Avaliação mínima é 1")
        @Max(value = 5, message = "Avaliação máxima é 5")
        private Integer rating;

        @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
        private String comment;
    }

    @Data
    @Builder
    public static class ReviewInfo {
        private String id;
        private Long restaurantId;
        private Long userId;
        private Long orderId;
        private Integer rating;
        private String comment;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static ReviewInfo from(Review review) {
            return ReviewInfo.builder()
                    .id(review.getId())
                    .restaurantId(review.getRestaurantId())
                    .userId(review.getUserId())
                    .orderId(review.getOrderId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .createdAt(review.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class RestaurantRatingSummary {
        private Long restaurantId;
        private Double averageRating;
        private Long totalReviews;
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
