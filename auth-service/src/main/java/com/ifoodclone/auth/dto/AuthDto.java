package com.ifoodclone.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.auth.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class AuthDto {

    private AuthDto() {
        // Construtor privado para evitar instanciação
    }

    @Data
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        private String password;

        private String deviceInfo;
        private String ipAddress;
    }

    @Data
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        private String password;

        private String firstName;
        private String lastName;
        private String phone;

        @NotNull(message = "Tipo de usuário é obrigatório")
        private User.UserRole role;
    }

    @Data
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;

        @Builder.Default
        private String tokenType = "Bearer";

        private Long expiresIn; // in seconds
        private UserInfo user;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime issuedAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private User.UserRole role;
        private String roleDisplayName;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private Boolean active;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastLoginAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token é obrigatório")
        private String refreshToken;

        private String deviceInfo;
        private String ipAddress;
    }

    @Data
    @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;

        @Builder.Default
        private String tokenType = "Bearer";

        private Long expiresIn; // in seconds

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime issuedAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    public static class PasswordResetRequest {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        private String email;
    }

    @Data
    @Builder
    public static class PasswordResetConfirmRequest {
        @NotBlank(message = "Token é obrigatório")
        private String token;

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        private String newPassword;
    }

    @Data
    @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Senha atual é obrigatória")
        private String currentPassword;

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, message = "Nova senha deve ter pelo menos 8 caracteres")
        private String newPassword;
    }

    @Data
    @Builder
    public static class EmailVerificationRequest {
        @NotBlank(message = "Token é obrigatório")
        private String token;
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