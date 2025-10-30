package com.ifoodclone.user.controller;

import java.util.HashMap;
import java.util.Map;

import com.ifoodclone.user.config.UserSecurityConfig.UserContext;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        Long userId = UserContext.getUserId();
        String userEmail = UserContext.getUserEmail();
        String userRoles = UserContext.getUserRoles();

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("email", userEmail);
        profile.put("roles", userRoles);
        profile.put("isAdmin", UserContext.isAdmin());
        profile.put("isCustomer", UserContext.isCustomer());
        profile.put("isRestaurantOwner", UserContext.isRestaurantOwner());

        return ResponseEntity.ok(profile);
    }

    /**
     * Admin-only endpoint
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        if (!UserContext.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("adminUser", UserContext.getUserEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile (owner or admin only)
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable Long userId) {
        Long currentUserId = UserContext.getUserId();

        // Only allow users to update their own profile or admins to update any
        if (!userId.equals(currentUserId) && !UserContext.isAdmin()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("updatedBy", currentUserId);
        response.put("targetUserId", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Restaurant owner specific endpoint
     */
    @GetMapping("/restaurant/dashboard")
    public ResponseEntity<Map<String, Object>> getRestaurantDashboard() {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Restaurant owner access required"));
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("ownerId", UserContext.getUserId());
        dashboard.put("ownerEmail", UserContext.getUserEmail());
        dashboard.put("dashboardType", "restaurant");

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Delivery driver specific endpoint
     */
    @GetMapping("/delivery/dashboard")
    public ResponseEntity<Map<String, Object>> getDeliveryDashboard() {
        if (!UserContext.isDeliveryDriver() && !UserContext.isAdmin()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Delivery driver access required"));
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("driverId", UserContext.getUserId());
        dashboard.put("driverEmail", UserContext.getUserEmail());
        dashboard.put("dashboardType", "delivery");

        return ResponseEntity.ok(dashboard);
    }
}