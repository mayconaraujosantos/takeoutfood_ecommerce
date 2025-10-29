package com.ifoodclone.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public List<GroupedOpenApi> apis() {
        List<GroupedOpenApi> groups = new ArrayList<>();

        // Authentication Service API
        groups.add(GroupedOpenApi.builder()
                .group("auth-service")
                .displayName("Authentication Service API v1")
                .pathsToMatch("/api/v1/auth/**")
                .build());

        // User Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("user-service")
                .displayName("User Management Service API v1")
                .pathsToMatch("/api/v1/users/**")
                .build());

        // Restaurant Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("restaurant-service")
                .displayName("Restaurant Management Service API v1")
                .pathsToMatch("/api/v1/restaurants/**")
                .build());

        // Menu Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("menu-service")
                .displayName("Menu Management Service API v1")
                .pathsToMatch("/api/v1/menus/**")
                .build());

        // Order Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("order-service")
                .displayName("Order Management Service API v1")
                .pathsToMatch("/api/v1/orders/**")
                .build());

        // Payment Processing Service API
        groups.add(GroupedOpenApi.builder()
                .group("payment-service")
                .displayName("Payment Processing Service API v1")
                .pathsToMatch("/api/v1/payments/**")
                .build());

        // Delivery Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("delivery-service")
                .displayName("Delivery Management Service API v1")
                .pathsToMatch("/api/v1/deliveries/**")
                .build());

        // Review Management Service API
        groups.add(GroupedOpenApi.builder()
                .group("review-service")
                .displayName("Review Management Service API v1")
                .pathsToMatch("/api/v1/reviews/**")
                .build());

        // Notification Service API
        groups.add(GroupedOpenApi.builder()
                .group("notification-service")
                .displayName("Notification Service API v1")
                .pathsToMatch("/api/v1/notifications/**")
                .build());

        return groups;
    }
}