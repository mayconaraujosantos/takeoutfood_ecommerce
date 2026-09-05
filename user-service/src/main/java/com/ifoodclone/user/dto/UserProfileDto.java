package com.ifoodclone.user.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.user.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserProfileDto {

    private UserProfileDto() {
    }

    // @Data + @Builder alone suppresses the no-args constructor Jackson needs to
    // deserialize a @RequestBody (Lombok only auto-generates one when no other
    // constructor exists, and @Builder's internal all-args constructor counts as one).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String bio;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class ProfileInfo {
        private Long userId;
        private String bio;
        private String avatarUrl;
        private Long defaultAddressId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static ProfileInfo from(UserProfile profile) {
            return ProfileInfo.builder()
                    .userId(profile.getUserId())
                    .bio(profile.getBio())
                    .avatarUrl(profile.getAvatarUrl())
                    .defaultAddressId(profile.getDefaultAddressId())
                    .createdAt(profile.getCreatedAt())
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
