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

        // Auth Service API
        GroupedOpenApi.builder()
                .group("auth-service")
                .pathsToMatch("/api/auth/**")
                .build();

        groups.add(GroupedOpenApi.builder()
                .group("auth-service")
                .displayName("Authentication Service")
                .pathsToMatch("/api/auth/**")
                .build());

        // User Service API (futuro)
        groups.add(GroupedOpenApi.builder()
                .group("user-service")
                .displayName("User Service")
                .pathsToMatch("/api/users/**")
                .build());

        // Restaurant Service API (futuro)
        groups.add(GroupedOpenApi.builder()
                .group("restaurant-service")
                .displayName("Restaurant Service")
                .pathsToMatch("/api/restaurants/**")
                .build());

        // Order Service API (futuro)
        groups.add(GroupedOpenApi.builder()
                .group("order-service")
                .displayName("Order Service")
                .pathsToMatch("/api/orders/**")
                .build());

        // Payment Service API (futuro)
        groups.add(GroupedOpenApi.builder()
                .group("payment-service")
                .displayName("Payment Service")
                .pathsToMatch("/api/payments/**")
                .build());

        return groups;
    }
}