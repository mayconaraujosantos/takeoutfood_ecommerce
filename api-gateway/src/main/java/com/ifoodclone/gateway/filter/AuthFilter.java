package com.ifoodclone.gateway.filter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().value();

            // Check if authentication is required
            if (shouldSkipAuth(config, requestPath)) {
                return chain.filter(exchange);
            }

            // Validate authorization header and extract token
            String token = extractToken(request);
            if (token == null) {
                log.warn("Missing or invalid authorization header for path: {}", requestPath);
                return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Validate and process token
            return validateAndProcessToken(exchange, chain, token, requestPath);
        };
    }

    private boolean shouldSkipAuth(Config config, String requestPath) {
        if (!config.isRequireAuth()) {
            log.debug("Authentication skipped for path: {}", requestPath);
            return true;
        }

        if (!config.getBypassPaths().isEmpty()) {
            return matchesBypassPath(config.getBypassPaths(), requestPath);
        }

        return false;
    }

    private boolean matchesBypassPath(String bypassPaths, String requestPath) {
        String[] paths = bypassPaths.split(",");
        for (String bypassPath : paths) {
            if (requestPath.matches(bypassPath.trim())) {
                log.debug("Authentication bypassed for path: {}", requestPath);
                return true;
            }
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return null;
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }

    private Mono<Void> validateAndProcessToken(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
            String token,
            String requestPath) {
        try {
            if (!isValidToken(token)) {
                log.warn("Invalid token for path: {}", requestPath);
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = getClaims(token);
            ServerHttpRequest modifiedRequest = buildAuthenticatedRequest(exchange.getRequest(), claims, requestPath);

            log.debug("User authenticated: userId={}, path={}", claims.getSubject(), requestPath);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error validating token for path {}: {}", requestPath, e.getMessage());
            return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private ServerHttpRequest buildAuthenticatedRequest(ServerHttpRequest request, Claims claims, String requestPath) {
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        String roles = claims.get("roles", String.class);
        String authorities = claims.get("authorities", String.class);

        return request.mutate()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-User-Email", email != null ? email : "")
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-User-Authorities", authorities != null ? authorities : "")
                .header("X-Authenticated", "true")
                .header("X-Request-Path", requestPath)
                .build();
    }

    private boolean isValidToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check if token is expired
            return claims.getExpiration() != null && claims.getExpiration().after(new java.util.Date());
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"timestamp\": \"%s\"}",
                message, java.time.Instant.now().toString());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        private boolean requireAuth = true;
        private String bypassPaths = "";

        public boolean isRequireAuth() {
            return requireAuth;
        }

        public void setRequireAuth(boolean requireAuth) {
            this.requireAuth = requireAuth;
        }

        public String getBypassPaths() {
            return bypassPaths;
        }

        public void setBypassPaths(String bypassPaths) {
            this.bypassPaths = bypassPaths;
        }
    }
}