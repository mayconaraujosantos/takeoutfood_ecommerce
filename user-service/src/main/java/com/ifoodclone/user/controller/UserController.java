package com.ifoodclone.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ifoodclone.user.config.UserSecurityConfig.UserContext;
import com.ifoodclone.user.dto.AddressDto;
import com.ifoodclone.user.dto.AddressDto.AddressInfo;
import com.ifoodclone.user.dto.UserProfileDto;
import com.ifoodclone.user.dto.UserProfileDto.ApiResponse;
import com.ifoodclone.user.entity.Address;
import com.ifoodclone.user.entity.UserProfile;
import com.ifoodclone.user.service.UserProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        Long userId = UserContext.getUserId();
        String userEmail = UserContext.getUserEmail();
        String userRoles = UserContext.getUserRoles();

        UserProfile profile = userProfileService.getOrCreateProfile(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("email", userEmail);
        response.put("roles", userRoles);
        response.put("isAdmin", UserContext.isAdmin());
        response.put("isCustomer", UserContext.isCustomer());
        response.put("isRestaurantOwner", UserContext.isRestaurantOwner());
        response.put("bio", profile.getBio());
        response.put("avatarUrl", profile.getAvatarUrl());
        response.put("defaultAddressId", profile.getDefaultAddressId());

        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse<UserProfileDto.ProfileInfo>> updateUserProfile(@PathVariable Long userId,
            @RequestBody UserProfileDto.UpdateRequest request) {
        Long currentUserId = UserContext.getUserId();

        // Only allow users to update their own profile or admins to update any
        if (!userId.equals(currentUserId) && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        UserProfile profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity
                .ok(ApiResponse.success("Profile updated successfully", UserProfileDto.ProfileInfo.from(profile)));
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

    /**
     * List addresses of the authenticated user
     */
    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressInfo>>> listMyAddresses() {
        List<AddressInfo> addresses = userProfileService.listAddresses(UserContext.getUserId()).stream()
                .map(AddressInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    /**
     * Add a new address for the authenticated user
     */
    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressInfo>> addMyAddress(
            @Valid @RequestBody AddressDto.CreateRequest request) {
        Address address = userProfileService.addAddress(UserContext.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Endereço adicionado com sucesso", AddressInfo.from(address)));
    }

    /**
     * Update an address of the authenticated user
     */
    @PutMapping("/me/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressInfo>> updateMyAddress(@PathVariable Long id,
            @RequestBody AddressDto.UpdateRequest request) {
        try {
            Address address = userProfileService.updateAddress(UserContext.getUserId(), id, request);
            return ResponseEntity
                    .ok(ApiResponse.success("Endereço atualizado com sucesso", AddressInfo.from(address)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Delete an address of the authenticated user
     */
    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMyAddress(@PathVariable Long id) {
        try {
            userProfileService.deleteAddress(UserContext.getUserId(), id);
            return ResponseEntity.ok(ApiResponse.success("Endereço removido com sucesso", null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Mark an address as the default one for the authenticated user
     */
    @PatchMapping("/me/addresses/{id}/default")
    public ResponseEntity<ApiResponse<AddressInfo>> setDefaultAddress(@PathVariable Long id) {
        try {
            Address address = userProfileService.setDefaultAddress(UserContext.getUserId(), id);
            return ResponseEntity
                    .ok(ApiResponse.success("Endereço padrão atualizado", AddressInfo.from(address)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
