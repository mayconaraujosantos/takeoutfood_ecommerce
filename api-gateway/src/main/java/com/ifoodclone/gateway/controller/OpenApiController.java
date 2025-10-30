package com.ifoodclone.gateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Mono;

/**
 * Controller to serve aggregated OpenAPI documentation from microservices
 */
@RestController
public class OpenApiController {

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final RestTemplate restTemplate;

    public OpenApiController(RouteDefinitionLocator routeDefinitionLocator, RestTemplate restTemplate) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns list of available services with OpenAPI documentation
     */
    @GetMapping("/v3/api-docs")
    public Mono<Map<String, Object>> getServicesList() {
        return routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .map(this::createServicesResponse);
    }

    /**
     * Returns OpenAPI specification for a specific service
     */
    @GetMapping("/v3/api-docs/{service}")
    public ResponseEntity<String> getServiceApiDocs(@PathVariable String service) {
        try {
            // Map service names to their actual service discovery names
            String serviceName = mapServiceName(service);
            String url = String.format("http://%s/v3/api-docs", serviceName);

            String apiDocs = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(apiDocs);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> createServicesResponse(List<RouteDefinition> routes) {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, String>> services = routes.stream()
                .filter(route -> route.getId().contains("-service"))
                .map(route -> {
                    Map<String, String> service = new HashMap<>();
                    service.put("name", route.getId());
                    service.put("url", "/v3/api-docs/" + route.getId());
                    return service;
                })
                .toList();

        response.put("services", services);
        return response;
    }

    private String mapServiceName(String service) {
        // Map gateway service names to actual service discovery names
        Map<String, String> serviceMapping = Map.of(
                "auth-service", "auth-service",
                "user-service", "user-service",
                "restaurant-service", "restaurant-service",
                "menu-service", "menu-service",
                "order-service", "order-service",
                "payment-service", "payment-service",
                "delivery-service", "delivery-service",
                "review-service", "review-service",
                "notification-service", "notification-service");

        return serviceMapping.getOrDefault(service, service);
    }
}