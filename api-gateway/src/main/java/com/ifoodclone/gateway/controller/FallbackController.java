package com.ifoodclone.gateway.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        log.warn("🔴 Auth service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Authentication service",
                "The authentication service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        log.warn("🔴 User service is unavailable - Circuit breaker activated");
        return createFallbackResponse("User service",
                "The user service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/restaurant")
    public ResponseEntity<Map<String, Object>> restaurantFallback() {
        log.warn("🔴 Restaurant service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Restaurant service",
                "The restaurant service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/menu")
    public ResponseEntity<Map<String, Object>> menuFallback() {
        log.warn("🔴 Menu service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Menu service",
                "The menu service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        log.warn("🔴 Order service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Order service",
                "The order service is temporarily unavailable. Your pending orders are safe and will be processed once the service is restored.");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        log.warn("🔴 Payment service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Payment service",
                "The payment service is temporarily unavailable. Please wait a moment before trying to make payments.");
    }

    @GetMapping("/delivery")
    public ResponseEntity<Map<String, Object>> deliveryFallback() {
        log.warn("🔴 Delivery service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Delivery service",
                "The delivery service is temporarily unavailable. Existing deliveries are being processed normally.");
    }

    @GetMapping("/review")
    public ResponseEntity<Map<String, Object>> reviewFallback() {
        log.warn("🔴 Review service is unavailable - Circuit breaker activated");
        return createFallbackResponse("Review service",
                "The review service is temporarily unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String service, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("service", service);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("suggestion", "Please check our status page or try again in a few moments");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}