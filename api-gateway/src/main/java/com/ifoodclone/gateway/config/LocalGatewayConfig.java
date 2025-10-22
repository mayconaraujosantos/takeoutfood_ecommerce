package com.ifoodclone.gateway.config;

import com.ifoodclone.gateway.filter.AuthFilter;
import com.ifoodclone.gateway.filter.LoggingFilter;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@Profile("local")
public class LocalGatewayConfig {

    private final AuthFilter authFilter;
    private final LoggingFilter loggingFilter;

    public LocalGatewayConfig(AuthFilter authFilter, LoggingFilter loggingFilter) {
        this.authFilter = authFilter;
        this.loggingFilter = loggingFilter;
    }

    @Bean
    public RouteLocator localRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Public endpoints (mock)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .addResponseHeader("X-Mock-Service", "auth-service"))
                        .uri("http://httpbin.org"))

                // User Service - Protected endpoints (mock)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(authFilter.apply(new AuthFilter.Config()))
                                .addResponseHeader("X-Mock-Service", "user-service"))
                        .uri("http://httpbin.org"))

                // Restaurant Service - Public endpoints (mock)
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .addResponseHeader("X-Mock-Service", "restaurant-service"))
                        .uri("http://httpbin.org"))

                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}