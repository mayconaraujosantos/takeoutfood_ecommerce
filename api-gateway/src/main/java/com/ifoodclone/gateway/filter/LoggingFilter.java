package com.ifoodclone.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String traceId = UUID.randomUUID().toString().substring(0, 8);

            // Log request
            log.info("🔍 [{}] {} {} - Headers: {}",
                    traceId,
                    request.getMethod(),
                    request.getURI(),
                    request.getHeaders().toSingleValueMap());

            // Add trace ID to headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Trace-ID", traceId)
                    .build();

            long startTime = System.currentTimeMillis();

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doOnSuccess(result -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("✅ [{}] Response completed in {}ms - Status: {}",
                                traceId,
                                duration,
                                exchange.getResponse().getStatusCode());
                    })
                    .doOnError(error -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("❌ [{}] Request failed after {}ms - Error: {}",
                                traceId,
                                duration,
                                error.getMessage());
                    });
        };
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}