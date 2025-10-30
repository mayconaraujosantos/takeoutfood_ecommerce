package com.ifoodclone.auth.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para testar tracing distribuído sem dependências externas
 */
@RestController
@RequestMapping("/api/test")
public class TracingTestController {

    private static final Logger logger = LoggerFactory.getLogger(TracingTestController.class);

    @Autowired
    private Environment environment;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Health check endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", environment.getProperty("server.port", "8081"));

        logger.info("Health check response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        logger.info("Login attempt for user: {}", email);

        // Simular processamento de login
        try {
            Thread.sleep(100); // Simular latência
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", email);
        response.put("token", "fake-jwt-token-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now());

        logger.info("Login successful for user: {}", email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userInfo) {
        String email = userInfo.get("email");
        String name = userInfo.get("name");

        logger.info("Registration attempt for user: {} with email: {}", name, email);

        // Simular processamento de registro
        try {
            Thread.sleep(200); // Simular latência maior
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", email);
        response.put("name", name);
        response.put("userId", System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now());

        logger.info("Registration successful for user: {} with email: {}", name, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        logger.info("Metrics endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("requests_total", 42);
        response.put("uptime_seconds", System.currentTimeMillis() / 1000);
        response.put("memory_used_mb", Runtime.getRuntime().totalMemory() / 1024 / 1024);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}