package com.ifoodclone.auth.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor para gerenciar Correlation IDs em requests distribuídos
 */
@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdInterceptor.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private final Tracer tracer;

    public CorrelationIdInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obter ou gerar Correlation ID
        String correlationId = getOrGenerateCorrelationId(request);

        // Obter ou gerar Request ID (único para cada request)
        String requestId = getOrGenerateRequestId(request);

        // Adicionar aos headers de resposta
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Adicionar ao MDC para logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        // Adicionar ao span atual se existir
        Span currentSpan = tracer.nextSpan();
        if (currentSpan != null) {
            currentSpan.tag("correlation.id", correlationId);
            currentSpan.tag("request.id", requestId);
            currentSpan.tag("http.method", request.getMethod());
            currentSpan.tag("http.url", request.getRequestURL().toString());
            currentSpan.tag("http.user_agent", request.getHeader("User-Agent"));
            currentSpan.tag("client.ip", getClientIp(request));
        }

        logger.info("Request started - Method: {} URL: {} CorrelationID: {} RequestID: {}",
                request.getMethod(), request.getRequestURL(), correlationId, requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);

        logger.info("Request completed - Status: {} CorrelationID: {} RequestID: {} Exception: {}",
                response.getStatus(), correlationId, requestId, ex != null ? ex.getMessage() : "none");

        // Limpar MDC
        MDC.remove(CORRELATION_ID_MDC_KEY);
        MDC.remove(REQUEST_ID_MDC_KEY);
    }

    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        // Verificar se já existe um Correlation ID no header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            // Gerar novo Correlation ID
            correlationId = generateCorrelationId();
            logger.debug("Generated new correlation ID: {}", correlationId);
        } else {
            logger.debug("Using existing correlation ID from header: {}", correlationId);
        }

        return correlationId;
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        // Verificar se já existe um Request ID no header
        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.trim().isEmpty()) {
            // Gerar novo Request ID (sempre único para cada request)
            requestId = generateRequestId();
            logger.debug("Generated new request ID: {}", requestId);
        } else {
            logger.debug("Using existing request ID from header: {}", requestId);
        }

        return requestId;
    }

    private String generateCorrelationId() {
        // Formato: timestamp-uuid (mais legível)
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateRequestId() {
        // Formato: req-timestamp-random
        return "req-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String getClientIp(HttpServletRequest request) {
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