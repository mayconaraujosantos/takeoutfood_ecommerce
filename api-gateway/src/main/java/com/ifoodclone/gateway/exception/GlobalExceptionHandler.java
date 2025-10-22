package com.ifoodclone.gateway.exception;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Order(-1) // High priority
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Set content type
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        HttpStatus status = determineStatus(ex);
        response.setStatusCode(status);

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().toString();

        Map<String, Object> errorResponse = createErrorResponse(ex, status, path, method);

        // Log error
        logError(ex, path, method, status);

        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            jsonResponse = "{\"error\":\"Internal server error\",\"message\":\"Unable to process error response\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        }

        if (ex.getMessage() != null) {
            String message = ex.getMessage().toLowerCase();
            if (message.contains("connection") || message.contains("timeout")) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
            if (message.contains("not found") || message.contains("404")) {
                return HttpStatus.NOT_FOUND;
            }
            if (message.contains("unauthorized") || message.contains("401")) {
                return HttpStatus.UNAUTHORIZED;
            }
            if (message.contains("forbidden") || message.contains("403")) {
                return HttpStatus.FORBIDDEN;
            }
            if (message.contains("bad request") || message.contains("400")) {
                return HttpStatus.BAD_REQUEST;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> createErrorResponse(Throwable ex, HttpStatus status, String path, String method) {
        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("path", path);
        errorResponse.put("method", method);

        // Customize message based on status
        switch (status) {
            case GATEWAY_TIMEOUT:
                errorResponse.put("message", "Service temporarily unavailable. Please try again later.");
                break;
            case SERVICE_UNAVAILABLE:
                errorResponse.put("message", "The requested service is currently unavailable.");
                break;
            case NOT_FOUND:
                errorResponse.put("message", "The requested resource was not found.");
                break;
            case UNAUTHORIZED:
                errorResponse.put("message", "Authentication is required to access this resource.");
                break;
            case FORBIDDEN:
                errorResponse.put("message", "You don't have permission to access this resource.");
                break;
            case BAD_REQUEST:
                errorResponse.put("message", "The request is invalid. Please check your input.");
                break;
            case TOO_MANY_REQUESTS:
                errorResponse.put("message", "Too many requests. Please slow down and try again later.");
                break;
            default:
                errorResponse.put("message", "An unexpected error occurred. Please try again later.");
        }

        // Add trace ID if available
        errorResponse.put("traceId", generateTraceId());

        return errorResponse;
    }

    private void logError(Throwable ex, String path, String method, HttpStatus status) {
        if (status.is5xxServerError()) {
            log.error("🚨 Server error on {} {}: {}", method, path, ex.getMessage(), ex);
        } else if (status.is4xxClientError() && !status.equals(HttpStatus.NOT_FOUND)) {
            log.warn("⚠️ Client error on {} {}: {}", method, path, ex.getMessage());
        } else {
            log.debug("ℹ️ Request error on {} {}: {}", method, path, ex.getMessage());
        }
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}