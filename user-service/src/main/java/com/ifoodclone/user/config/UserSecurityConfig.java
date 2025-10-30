package com.ifoodclone.user.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class UserSecurityConfig {

    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }

    public static class UserContextFilter extends OncePerRequestFilter {

        private static final List<String> EXCLUDED_PATHS = Arrays.asList(
                "/actuator/health",
                "/actuator/info",
                "/api-docs",
                "/swagger-ui");

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                @NonNull HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            String path = request.getRequestURI();

            // Skip authentication for excluded paths
            if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user information from headers (injected by API Gateway)
            String userId = request.getHeader("X-User-Id");
            String userEmail = request.getHeader("X-User-Email");
            String userRoles = request.getHeader("X-User-Roles");
            String authenticated = request.getHeader("X-Authenticated");

            // Check if user is authenticated
            if (!"true".equals(authenticated) || userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication required\"}");
                return;
            }

            // Store user context for this request
            UserContext.setUserId(Long.parseLong(userId));
            UserContext.setUserEmail(userEmail);
            UserContext.setUserRoles(userRoles);

            try {
                filterChain.doFilter(request, response);
            } finally {
                // Clear context after request
                UserContext.clear();
            }
        }
    }

    // Thread-local user context
    public static class UserContext {
        private static final ThreadLocal<Long> userId = new ThreadLocal<>();
        private static final ThreadLocal<String> userEmail = new ThreadLocal<>();
        private static final ThreadLocal<String> userRoles = new ThreadLocal<>();

        public static Long getUserId() {
            return userId.get();
        }

        public static void setUserId(Long id) {
            userId.set(id);
        }

        public static String getUserEmail() {
            return userEmail.get();
        }

        public static void setUserEmail(String email) {
            userEmail.set(email);
        }

        public static String getUserRoles() {
            return userRoles.get();
        }

        public static void setUserRoles(String roles) {
            userRoles.set(roles);
        }

        public static boolean hasRole(String role) {
            String roles = getUserRoles();
            return roles != null && roles.contains(role);
        }

        public static boolean isAdmin() {
            return hasRole("ADMIN");
        }

        public static boolean isCustomer() {
            return hasRole("CUSTOMER");
        }

        public static boolean isRestaurantOwner() {
            return hasRole("RESTAURANT_OWNER");
        }

        public static boolean isDeliveryDriver() {
            return hasRole("DELIVERY_DRIVER");
        }

        public static void clear() {
            userId.remove();
            userEmail.remove();
            userRoles.remove();
        }
    }
}