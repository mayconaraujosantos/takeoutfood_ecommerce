package com.ifoodclone.user.config;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private final Tracer tracer;

    public CorrelationIdInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obtém ou gera Correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Obtém ou gera Request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Define no MDC para logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        // Adiciona aos headers de resposta
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Adiciona tags ao span atual
        Span currentSpan = tracer.nextSpan();
        if (currentSpan != null) {
            currentSpan.tag("correlation.id", correlationId);
            currentSpan.tag("request.id", requestId);
            currentSpan.tag("service.name", "user-service");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        // Limpa o MDC
        MDC.remove(CORRELATION_ID_MDC_KEY);
        MDC.remove(REQUEST_ID_MDC_KEY);
    }
}