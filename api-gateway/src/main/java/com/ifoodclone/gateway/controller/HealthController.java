package com.ifoodclone.gateway.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        response.put("port", serverPort);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "API Gateway is running successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("description", "iFood Clone API Gateway");
        response.put("version", "1.0.0");
        response.put("java_version", System.getProperty("java.version"));
        response.put("profiles", System.getProperty("spring.profiles.active", "default"));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> routes() {
        Map<String, Object> response = new HashMap<>();
        response.put("available_routes", Map.of(
                "auth", "/api/auth/** - Authentication endpoints",
                "users", "/api/users/** - User management (protected)",
                "restaurants", "/api/restaurants/** - Restaurant management",
                "menus", "/api/menus/** - Menu management",
                "orders", "/api/orders/** - Order management (protected)",
                "payments", "/api/payments/** - Payment processing (protected)",
                "deliveries", "/api/deliveries/** - Delivery tracking (protected)",
                "reviews", "/api/reviews/** - Review system"));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}