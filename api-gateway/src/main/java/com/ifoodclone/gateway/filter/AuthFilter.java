package com.ifoodclone.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${app.jwt.secret:${JWT_SECRET:}}")
    private String jwtSecret;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            try {
                if (!isValidToken(token)) {
                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                }
                
                Claims claims = getClaims(token);
                // Prefer userId claim if present, otherwise fallback to subject
                Object userIdClaim = claims.get("userId");
                String userIdHeader = userIdClaim != null ? String.valueOf(userIdClaim) : claims.getSubject();

                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userIdHeader)
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (Exception e) {
                log.error("Error validating token: {}", e.getMessage());
                return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isValidToken(String token) {
        try {
            // Accept base64-encoded secret (same format as auth-service) or raw bytes
            byte[] keyBytes;
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                return false;
            }
            try {
                // Try base64 decode first (this matches auth-service JwtService)
                keyBytes = Decoders.BASE64.decode(jwtSecret);
            } catch (Exception ex) {
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }

            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception ex) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
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
        
        String body = String.format("{\"error\": \"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties if needed
    }
}