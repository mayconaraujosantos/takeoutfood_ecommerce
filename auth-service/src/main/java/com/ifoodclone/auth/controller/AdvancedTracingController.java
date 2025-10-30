package com.ifoodclone.auth.controller;

import java.util.HashMap;
import java.util.Map;

import com.ifoodclone.auth.service.CustomMetricsService;
import com.ifoodclone.auth.service.TracingDemoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller para demonstrar tracing avançado com anotações customizadas
 */
@RestController
@RequestMapping("/api/tracing")
public class AdvancedTracingController {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedTracingController.class);

    private final TracingDemoService tracingDemoService;
    private final CustomMetricsService customMetricsService;

    public AdvancedTracingController(TracingDemoService tracingDemoService,
            CustomMetricsService customMetricsService) {
        this.tracingDemoService = tracingDemoService;
        this.customMetricsService = customMetricsService;
    }

    @PostMapping("/validate")
    @NewSpan("validate-user-endpoint")
    public ResponseEntity<Map<String, Object>> validateUser(
            @RequestBody Map<String, String> request,
            @SpanTag("http.client_ip") HttpServletRequest servletRequest) {

        String email = request.get("email");
        String operation = request.getOrDefault("operation", "VALIDATE");
        String clientIp = getClientIp(servletRequest);

        logger.info("Received validation request for email: {} from IP: {}", email, clientIp);

        Map<String, Object> response = new HashMap<>();

        try {
            // Chamar service com spans customizados
            Map<String, Object> validationResult = tracingDemoService.validateUser(email, operation);

            // Registrar métricas de sucesso
            customMetricsService.recordValidation(true, email);

            response.put("success", true);
            response.put("data", validationResult);
            response.put("clientIp", clientIp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Validation failed for email: {}", email, e);

            // Registrar métricas de erro
            customMetricsService.recordValidation(false, email);

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("clientIp", clientIp);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/authenticate")
    @NewSpan("authenticate-endpoint")
    public ResponseEntity<Map<String, Object>> authenticate(
            @RequestBody Map<String, String> request,
            @SpanTag("auth.method") @RequestParam(defaultValue = "PASSWORD") String method,
            HttpServletRequest servletRequest) {

        String email = request.get("email");
        String password = request.get("password");
        String clientIp = getClientIp(servletRequest);

        logger.info("Authentication attempt for email: {} using method: {} from IP: {}",
                email, method, clientIp);

        Map<String, Object> response = new HashMap<>();

        try {
            // Processar autenticação com spans customizados
            Map<String, Object> authResult = tracingDemoService.processAuthentication(
                    method, email, clientIp);

            response.put("success", true);
            response.put("auth", authResult);
            response.put("method", method);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Authentication failed for email: {} with method: {}", email, method, e);

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("method", method);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/business/{operation}")
    @NewSpan("business-operation-endpoint")
    public ResponseEntity<Map<String, Object>> processBusinessOperation(
            @SpanTag("business.operation") @PathVariable String operation,
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest servletRequest) {

        String userEmail = (String) requestData.get("userEmail");
        Integer dataSize = (Integer) requestData.getOrDefault("dataSize", 0);
        String clientIp = getClientIp(servletRequest);

        logger.info("Processing business operation: {} for user: {} with data size: {} from IP: {}",
                operation, userEmail, dataSize, clientIp);

        Map<String, Object> response = new HashMap<>();

        try {
            // Processar lógica de negócio com spans customizados
            Map<String, Object> businessResult = tracingDemoService.processBusinessLogic(
                    operation.toUpperCase(), userEmail, dataSize);

            response.put("success", true);
            response.put("result", businessResult);
            response.put("operation", operation);
            response.put("clientIp", clientIp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Business operation failed: {} for user: {}", operation, userEmail, e);

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("operation", operation);
            response.put("clientIp", clientIp);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/chain-test")
    @NewSpan("chain-test-endpoint")
    public ResponseEntity<Map<String, Object>> chainTest(
            @SpanTag("chain.user") @RequestParam String userEmail,
            HttpServletRequest servletRequest) {

        String clientIp = getClientIp(servletRequest);
        logger.info("Starting chain test for user: {} from IP: {}", userEmail, clientIp);

        Map<String, Object> response = new HashMap<>();

        try {
            // Executar múltiplas operações em cadeia para criar span tree complexo
            Map<String, Object> validationResult = tracingDemoService.validateUser(userEmail, "CHAIN_TEST");
            Map<String, Object> authResult = tracingDemoService.processAuthentication("TOKEN", userEmail, clientIp);
            Map<String, Object> readResult = tracingDemoService.processBusinessLogic("READ", userEmail, 10);
            Map<String, Object> updateResult = tracingDemoService.processBusinessLogic("UPDATE", userEmail, 5);

            response.put("success", true);
            response.put("chainResults", Map.of(
                    "validation", validationResult,
                    "authentication", authResult,
                    "read", readResult,
                    "update", updateResult));
            response.put("userEmail", userEmail);
            response.put("clientIp", clientIp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Chain test failed for user: {}", userEmail, e);

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("userEmail", userEmail);
            response.put("clientIp", clientIp);

            return ResponseEntity.badRequest().body(response);
        }
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