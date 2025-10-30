package com.ifoodclone.gateway.filter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Profile("local")
public class SimpleRateLimitFilter extends AbstractGatewayFilterFactory<SimpleRateLimitFilter.Config> {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public SimpleRateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Use IP address as key for rate limiting
            String clientIp = getClientIp(request);
            String key = clientIp + ":" + request.getPath().value();

            AtomicInteger counter = counters.computeIfAbsent(key, k -> new AtomicInteger(0));

            if (counter.incrementAndGet() > config.getLimit()) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, request.getPath());
                return onError(exchange, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
            }

            // Simple cleanup every 100 requests
            if (counter.get() % 100 == 0) {
                counters.clear(); // Reset counters periodically
            }

            return chain.filter(exchange);
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return "localhost"; // Default for local testing
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                status.getReasonPhrase(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Data
    @NoArgsConstructor
    public static class Config {
        private int limit = 100;
        private Duration window = Duration.ofMinutes(1);

        public Config(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }
}