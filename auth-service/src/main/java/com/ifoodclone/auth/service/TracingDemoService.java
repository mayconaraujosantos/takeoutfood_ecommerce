package com.ifoodclone.auth.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

/**
 * Service para demonstrar custom spans e anotações de tracing
 */
@Service
public class TracingDemoService {

    private static final Logger logger = LoggerFactory.getLogger(TracingDemoService.class);
    private final Random random = new Random();

    @NewSpan("user-validation")
    public Map<String, Object> validateUser(@SpanTag("user.email") String email,
            @SpanTag("user.operation") String operation) {
        logger.info("Validating user: {} for operation: {}", email, operation);

        // Simular processamento com latência variável
        simulateProcessing("validation", 50, 200);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("email", email);
        result.put("operation", operation);
        result.put("validatedAt", LocalDateTime.now());

        logger.info("User validation completed for: {}", email);
        return result;
    }

    @NewSpan("authentication-process")
    public Map<String, Object> processAuthentication(
            @SpanTag("auth.type") String authType,
            @SpanTag("auth.user") String userEmail,
            @SpanTag("auth.ip") String clientIp) {

        logger.info("Processing authentication for user: {} with type: {} from IP: {}",
                userEmail, authType, clientIp);

        // Simular múltiplos passos de autenticação
        checkCredentials(userEmail);
        validatePermissions(userEmail);
        generateSession(userEmail);

        Map<String, Object> authResult = new HashMap<>();
        authResult.put("success", true);
        authResult.put("userEmail", userEmail);
        authResult.put("authType", authType);
        authResult.put("sessionId", "session-" + System.currentTimeMillis());
        authResult.put("authenticatedAt", LocalDateTime.now());

        logger.info("Authentication process completed for user: {}", userEmail);
        return authResult;
    }

    @NewSpan("credentials-check")
    private boolean checkCredentials(@SpanTag("user.email") String email) {
        logger.info("Checking credentials for user: {}", email);
        simulateProcessing("credential-check", 30, 100);

        // Simular falha ocasional para demonstrar error tracing
        if (random.nextInt(10) == 0) {
            logger.warn("Simulated credential check failure for user: {}", email);
            throw new RuntimeException("Simulated credential validation failure");
        }

        return true;
    }

    @NewSpan("permissions-validation")
    private boolean validatePermissions(@SpanTag("user.email") String email) {
        logger.info("Validating permissions for user: {}", email);
        simulateProcessing("permission-check", 20, 80);
        return true;
    }

    @NewSpan("session-generation")
    private String generateSession(@SpanTag("user.email") String email) {
        logger.info("Generating session for user: {}", email);
        simulateProcessing("session-gen", 40, 120);

        String sessionId = "session-" + System.currentTimeMillis() + "-" + email.hashCode();
        logger.info("Generated session: {} for user: {}", sessionId, email);
        return sessionId;
    }

    @NewSpan("business-logic")
    public Map<String, Object> processBusinessLogic(
            @SpanTag("business.operation") String operation,
            @SpanTag("business.user") String userEmail,
            @SpanTag("business.data_size") int dataSize) {

        logger.info("Processing business logic: {} for user: {} with data size: {}",
                operation, userEmail, dataSize);

        // Simular diferentes tipos de processamento baseado na operação
        switch (operation) {
            case "CREATE":
                return processCreate(userEmail, dataSize);
            case "UPDATE":
                return processUpdate(userEmail, dataSize);
            case "DELETE":
                return processDelete(userEmail);
            default:
                return processRead(userEmail);
        }
    }

    @NewSpan("create-operation")
    private Map<String, Object> processCreate(@SpanTag("user.email") String email,
            @SpanTag("data.size") int size) {
        simulateProcessing("create", 100, 300);
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "CREATE");
        result.put("created", true);
        result.put("id", random.nextLong());
        return result;
    }

    @NewSpan("update-operation")
    private Map<String, Object> processUpdate(@SpanTag("user.email") String email,
            @SpanTag("data.size") int size) {
        simulateProcessing("update", 80, 250);
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "UPDATE");
        result.put("updated", true);
        result.put("modifiedRows", random.nextInt(10) + 1);
        return result;
    }

    @NewSpan("delete-operation")
    private Map<String, Object> processDelete(@SpanTag("user.email") String email) {
        simulateProcessing("delete", 50, 150);
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "DELETE");
        result.put("deleted", true);
        result.put("deletedRows", random.nextInt(5) + 1);
        return result;
    }

    @NewSpan("read-operation")
    private Map<String, Object> processRead(@SpanTag("user.email") String email) {
        simulateProcessing("read", 30, 100);
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "READ");
        result.put("recordsFound", random.nextInt(100) + 1);
        return result;
    }

    /**
     * Simula processamento com latência variável
     */
    private void simulateProcessing(String operation, int minMs, int maxMs) {
        try {
            int delay = random.nextInt(maxMs - minMs) + minMs;
            logger.debug("Simulating {} processing for {}ms", operation, delay);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Processing interrupted for operation: {}", operation);
        }
    }
}