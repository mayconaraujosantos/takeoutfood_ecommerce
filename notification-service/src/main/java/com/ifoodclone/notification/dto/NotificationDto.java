package com.ifoodclone.notification.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.notification.entity.Notification;

import lombok.Builder;
import lombok.Data;

public class NotificationDto {

    private NotificationDto() {
    }

    @Data
    @Builder
    public static class NotificationInfo {
        private String id;
        private Notification.NotificationType type;
        private String title;
        private String message;
        private Long referenceId;
        private String sourceStatus;
        private boolean read;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static NotificationInfo from(Notification notification) {
            return NotificationInfo.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .referenceId(notification.getReferenceId())
                    .sourceStatus(notification.getSourceStatus())
                    .read(notification.isRead())
                    .createdAt(notification.getCreatedAt())
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
    }
}
