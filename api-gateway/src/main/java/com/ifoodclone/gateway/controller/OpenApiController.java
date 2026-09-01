package com.ifoodclone.gateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Controller to serve aggregated OpenAPI documentation from microservices
 */
@RestController
public class OpenApiController {

    // Ports match each service's fixed server.port (see each module's application.yml /
    // config-server's config/*.yml) - container hostnames match the docker-compose service names.
    private static final Map<String, Integer> SERVICE_PORTS = Map.ofEntries(
            Map.entry("auth-service", 8081),
            Map.entry("user-service", 8082),
            Map.entry("restaurant-service", 8083),
            Map.entry("menu-service", 8084),
            Map.entry("order-service", 8085),
            Map.entry("payment-service", 8086),
            Map.entry("notification-service", 8087),
            Map.entry("delivery-service", 8088),
            Map.entry("review-service", 8089));

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final WebClient.Builder webClientBuilder;

    public OpenApiController(RouteDefinitionLocator routeDefinitionLocator, WebClient.Builder webClientBuilder) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.webClientBuilder = webClientBuilder;
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
     * Returns OpenAPI specification for a specific service, called directly on its
     * fixed container hostname:port (same ones docker-compose/gitops wire up).
     */
    @GetMapping("/v3/api-docs/{service}")
    public Mono<ResponseEntity<String>> getServiceApiDocs(@PathVariable("service") String service) {
        String serviceName = mapServiceName(service);
        Integer port = SERVICE_PORTS.get(serviceName);
        if (port == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        return webClientBuilder.build()
                .get()
                .uri("http://{service}:{port}/v3/api-docs", serviceName, port)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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
        // Route ids are the Eureka service id already, except auth-service's
        // split public/protected routes, which both point at the same instance.
        if (service.startsWith("auth-service")) {
            return "auth-service";
        }
        return service;
    }
}