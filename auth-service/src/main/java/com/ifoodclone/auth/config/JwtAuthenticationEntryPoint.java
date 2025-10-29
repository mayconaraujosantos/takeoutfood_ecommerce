package com.ifoodclone.auth.config;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifoodclone.auth.dto.AuthDto;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String path = request.getRequestURI();
        String message = determineErrorMessage(authException, path);

        AuthDto.ApiResponse<Object> errorResponse = AuthDto.ApiResponse.builder()
                .success(false)
                .message("Acesso não autorizado")
                .error(message)
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String determineErrorMessage(AuthenticationException authException, String path) {
        String message = authException.getMessage();

        if (message == null || message.isEmpty()) {
            message = "Token de acesso inválido ou expirado";
        }

        // Personalizar mensagem baseada no caminho
        if (path.contains("/admin")) {
            return "Acesso de administrador necessário: " + message;
        } else if (path.contains("/restaurant")) {
            return "Acesso de restaurante necessário: " + message;
        } else if (path.contains("/delivery")) {
            return "Acesso de entregador necessário: " + message;
        }

        return message;
    }
}