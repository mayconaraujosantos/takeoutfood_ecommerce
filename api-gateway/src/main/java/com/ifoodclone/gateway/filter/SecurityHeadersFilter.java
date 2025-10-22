package com.ifoodclone.gateway.filter;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SecurityHeadersFilter extends AbstractGatewayFilterFactory<SecurityHeadersFilter.Config> {

    private static final List<String> SUSPICIOUS_PATTERNS = Arrays.asList(
            "script", "javascript", "onload", "onerror", "eval",
            "alert", "document.cookie", "window.location");

    public SecurityHeadersFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check for suspicious content in headers
            if (containsSuspiciousContent(request)) {
                return onError(exchange, "Suspicious content detected", HttpStatus.BAD_REQUEST);
            }

            // Add security headers to response
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();

                // Security headers
                response.getHeaders().add("X-Content-Type-Options", "nosniff");
                response.getHeaders().add("X-Frame-Options", "DENY");
                response.getHeaders().add("X-XSS-Protection", "1; mode=block");
                response.getHeaders().add("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains; preload");
                response.getHeaders().add("Content-Security-Policy",
                        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';");
                response.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
                response.getHeaders().add("Permissions-Policy",
                        "camera=(), microphone=(), location=(), payment=()");

                // Remove server information
                response.getHeaders().remove("Server");
            }));
        };
    }

    private boolean containsSuspiciousContent(ServerHttpRequest request) {
        // Check User-Agent
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent != null && containsSuspiciousPatterns(userAgent.toLowerCase())) {
            log.warn("Suspicious User-Agent detected: {}", userAgent);
            return true;
        }

        // Check custom headers for injection attempts
        for (String headerName : request.getHeaders().keySet()) {
            if (headerName.toLowerCase().contains("script") ||
                    headerName.toLowerCase().contains("inject")) {
                log.warn("Suspicious header name detected: {}", headerName);
                return true;
            }

            List<String> headerValues = request.getHeaders().get(headerName);
            if (headerValues != null) {
                for (String value : headerValues) {
                    if (containsSuspiciousPatterns(value.toLowerCase())) {
                        log.warn("Suspicious content in header {}: {}", headerName, value);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean containsSuspiciousPatterns(String content) {
        return SUSPICIOUS_PATTERNS.stream()
                .anyMatch(pattern -> content.contains(pattern.toLowerCase()));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                status.getReasonPhrase(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}