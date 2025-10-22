package com.ifoodclone.auth.controller;

import java.security.Principal;

import com.ifoodclone.auth.dto.AuthDto;
import com.ifoodclone.auth.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Authentication", description = "APIs de Autenticação e Autorização")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpRequest) {

        // Adicionar informações do device e IP
        request.setDeviceInfo(extractDeviceInfo(httpRequest));
        request.setIpAddress(extractIpAddress(httpRequest));

        try {
            AuthDto.LoginResponse response = authService.login(request);
            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Login realizado com sucesso", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDto.ApiResponse.error("Falha no login", ex.getMessage()));
        }
    }

    /**
     * Registro de novo usuário
     */
    @PostMapping("/register")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.UserInfo>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {

        try {
            AuthDto.UserInfo userInfo = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthDto.ApiResponse.success("Usuário registrado com sucesso", userInfo));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha no registro", ex.getMessage()));
        }
    }

    /**
     * Renovar access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.TokenResponse>> refreshToken(
            @Valid @RequestBody AuthDto.RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        // Adicionar informações do device e IP
        request.setDeviceInfo(extractDeviceInfo(httpRequest));
        request.setIpAddress(extractIpAddress(httpRequest));

        try {
            AuthDto.TokenResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Token renovado com sucesso", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDto.ApiResponse.error("Falha ao renovar token", ex.getMessage()));
        }
    }

    /**
     * Logout do usuário
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDto.ApiResponse<Void>> logout(
            @RequestBody(required = false) AuthDto.RefreshTokenRequest request) {

        try {
            String refreshToken = request != null ? request.getRefreshToken() : null;
            authService.logout(refreshToken);

            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Logout realizado com sucesso", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha no logout", ex.getMessage()));
        }
    }

    /**
     * Logout de todos os dispositivos
     */
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDto.ApiResponse<Void>> logoutFromAllDevices(Principal principal) {
        try {
            Long userId = getCurrentUserId();
            authService.logoutFromAllDevices(userId);

            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Logout de todos os dispositivos realizado com sucesso", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha no logout", ex.getMessage()));
        }
    }

    /**
     * Trocar senha
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDto.ApiResponse<Void>> changePassword(
            @Valid @RequestBody AuthDto.ChangePasswordRequest request) {

        try {
            Long userId = getCurrentUserId();
            authService.changePassword(userId, request);

            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Senha alterada com sucesso", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha ao alterar senha", ex.getMessage()));
        }
    }

    /**
     * Solicitar reset de senha
     */
    @PostMapping("/password/reset")
    public ResponseEntity<AuthDto.ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody AuthDto.PasswordResetRequest request) {

        try {
            // TODO: Implementar serviço de reset de senha
            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Email de reset de senha enviado", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha ao solicitar reset", ex.getMessage()));
        }
    }

    /**
     * Confirmar reset de senha
     */
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<AuthDto.ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody AuthDto.PasswordResetConfirmRequest request) {

        try {
            // TODO: Implementar confirmação de reset de senha
            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Senha alterada com sucesso", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha ao alterar senha", ex.getMessage()));
        }
    }

    /**
     * Verificar email
     */
    @PostMapping("/email/verify")
    public ResponseEntity<AuthDto.ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody AuthDto.EmailVerificationRequest request) {

        try {
            // TODO: Implementar verificação de email
            return ResponseEntity.ok(
                    AuthDto.ApiResponse.success("Email verificado com sucesso", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthDto.ApiResponse.error("Falha na verificação de email", ex.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<AuthDto.ApiResponse<String>> health() {
        return ResponseEntity.ok(
                AuthDto.ApiResponse.success("Auth service is running", "OK"));
    }

    /**
     * Extrair ID do usuário atual do contexto de segurança
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.ifoodclone.auth.entity.User) {
            com.ifoodclone.auth.entity.User user = (com.ifoodclone.auth.entity.User) authentication.getPrincipal();
            return user.getId();
        }
        throw new RuntimeException("Usuário não autenticado");
    }

    /**
     * Extrair informações do device
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown Device";
        }

        // Simplificar user agent para device info
        if (userAgent.contains("Mobile")) {
            return "Mobile Device";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * Extrair endereço IP
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}