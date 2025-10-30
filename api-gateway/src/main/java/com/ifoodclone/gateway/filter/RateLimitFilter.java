package com.ifoodclone.gateway.filter;

import java.time.Duration;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Use IP address as key for rate limiting
            String clientIp = getClientIp(request);
            String key = "rate_limit:" + clientIp + ":" + request.getPath().value();

            return redisTemplate.opsForValue()
                    .get(key)
                    .defaultIfEmpty("0")
                    .cast(String.class)
                    .flatMap(currentCount -> {
                        int count = Integer.parseInt(currentCount);

                        if (count >= config.getLimit()) {
                            return onError(exchange, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                        }

                        // Increment counter
                        return redisTemplate.opsForValue()
                                .increment(key)
                                .flatMap(newCount -> {
                                    if (newCount == 1) {
                                        // Set expiration for the first request
                                        return redisTemplate.expire(key, config.getWindow())
                                                .then(chain.filter(exchange));
                                    } else {
                                        return chain.filter(exchange);
                                    }
                                });
                    })
                    .onErrorResume(error -> {
                        log.warn("Rate limiting failed, allowing request: {}", error.getMessage());
                        return chain.filter(exchange);
                    });
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

        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
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
        private int limit = 10;
        private Duration window = Duration.ofMinutes(1);

        public Config(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }
}