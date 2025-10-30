package com.ifoodclone.auth.service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@Service
public class CustomMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomMetricsService.class);

    private final Counter validationSuccessCounter;
    private final Counter validationErrorCounter;
    private final Timer businessOperationTimer;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final String serviceName = "auth-service";

    public CustomMetricsService(MeterRegistry meterRegistry) {
        // Counter para sucessos de validação
        this.validationSuccessCounter = Counter.builder("auth.validation.success")
                .description("Number of successful validations")
                .tag("service", "auth")
                .register(meterRegistry);

        // Counter para erros de validação
        this.validationErrorCounter = Counter.builder("auth.validation.error")
                .description("Number of failed validations")
                .tag("service", "auth")
                .register(meterRegistry);

        // Timer para operações de negócio
        this.businessOperationTimer = Timer.builder("auth.business.operation.duration")
                .description("Duration of business operations")
                .tag("service", "auth")
                .register(meterRegistry);

        // Gauge para conexões ativas
        Gauge.builder("auth.active.connections", activeConnections, AtomicInteger::get)
                .description("Number of active connections")
                .tag("service", "auth")
                .register(meterRegistry);
    }

    @NewSpan("metrics-validation")
    public void recordValidation(@SpanTag("validation.result") boolean success,
            @SpanTag("validation.email") String email) {
        logger.info("Recording validation metric for email: {} with result: {}", email, success);

        if (success) {
            validationSuccessCounter.increment();
        } else {
            validationErrorCounter.increment();
        }
    }

    @NewSpan("metrics-business-operation")
    public void recordBusinessOperation(@SpanTag("operation.type") String operationType) {
        logger.info("Recording business operation metric for type: {}", operationType);

        Timer.Sample sample = Timer.start();

        try {
            // Simula operação de negócio com latência variável
            int latencyMs = ThreadLocalRandom.current().nextInt(50, 500);
            Thread.sleep(latencyMs);

            // Simula possível erro baseado na latência
            if (latencyMs > 400) {
                throw new RuntimeException("Simulated high latency error");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Business operation failed: {}", e.getMessage());
        } finally {
            sample.stop(businessOperationTimer);
        }
    }

    @NewSpan("metrics-connection-tracking")
    public void trackConnection(@SpanTag("connection.action") String action) {
        logger.info("Tracking connection action: {}", action);

        switch (action) {
            case "open":
                activeConnections.incrementAndGet();
                break;
            case "close":
                activeConnections.decrementAndGet();
                break;
            default:
                logger.warn("Unknown connection action: {}", action);
        }
    }

    @NewSpan("metrics-custom-latency-alert")
    public void checkLatencyAlert(@SpanTag("operation.latency") long latencyMs) {
        logger.info("Checking latency alert for duration: {}ms", latencyMs);

        // Simula alertas baseados em latência
        if (latencyMs > 1000) {
            logger.error("ALERT: High latency detected - {}ms (threshold: 1000ms)", latencyMs);
            // Em um cenário real, aqui seria enviado um alerta
        } else if (latencyMs > 500) {
            logger.warn("WARNING: Elevated latency detected - {}ms (threshold: 500ms)", latencyMs);
        }
    }

    // Métodos para simular diferentes cenários de métricas
    @NewSpan("simulate-high-load")
    public void simulateHighLoad() {
        logger.info("Simulating high load scenario");

        for (int i = 0; i < 10; i++) {
            recordValidation(ThreadLocalRandom.current().nextBoolean(), "load-test-" + i + "@example.com");
            recordBusinessOperation("load-test-operation");
            trackConnection("open");
        }
    }

    @NewSpan("simulate-error-scenario")
    public void simulateErrorScenario() {
        logger.info("Simulating error scenario");

        for (int i = 0; i < 5; i++) {
            recordValidation(false, "error-test-" + i + "@example.com");
            recordBusinessOperation("error-operation");
        }
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }
}