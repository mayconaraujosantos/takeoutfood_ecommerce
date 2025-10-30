package com.ifoodclone.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for User Service
 * 
 * In our microservices architecture:
 * - API Gateway handles JWT authentication and authorization
 * - User Service trusts requests that reach it through the gateway
 * - No JWT validation needed here (already done at gateway level)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints for monitoring and documentation
                        .requestMatchers(
                                "/actuator/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/h2-console/**")
                        .permitAll()

                        // All user service endpoints are open
                        // API Gateway handles authentication and passes user context via headers
                        .requestMatchers("/api/users/**").permitAll()

                        // Allow all other requests (trust the gateway)
                        .anyRequest().permitAll());

        return http.build();
    }
}