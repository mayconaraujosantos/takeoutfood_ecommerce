package com.ifoodclone.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ifoodclone.user.config.SecurityConfig;
import com.ifoodclone.user.config.TestConfig;
import com.ifoodclone.user.config.UserSecurityConfig;
import com.ifoodclone.user.config.UserSecurityConfig.UserContext;
import com.ifoodclone.user.entity.Address;
import com.ifoodclone.user.entity.UserProfile;
import com.ifoodclone.user.service.UserProfileService;

@WebMvcTest(controllers = UserController.class)
@Import({ TestConfig.class, SecurityConfig.class, UserSecurityConfig.class })
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("GET /api/users/profile returns persisted profile fields merged with JWT claims")
    void shouldReturnProfile() throws Exception {
        when(userProfileService.getOrCreateProfile(1L))
                .thenReturn(UserProfile.builder().userId(1L).bio("Olá").build());

        mockMvc.perform(get("/api/users/profile")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Email", "test@example.com")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Olá"));
    }

    @Test
    @DisplayName("PUT /api/users/profile/{userId} rejects updates from a different, non-admin user")
    void shouldRejectUpdateFromNonOwner() throws Exception {
        mockMvc.perform(put("/api/users/profile/2")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bio\":\"hack\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/profile/{userId} persists the update for the owner")
    void shouldUpdateOwnProfile() throws Exception {
        when(userProfileService.updateProfile(eq(1L), any()))
                .thenReturn(UserProfile.builder().userId(1L).bio("Novo bio").build());

        mockMvc.perform(put("/api/users/profile/1")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bio\":\"Novo bio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bio").value("Novo bio"));
    }

    @Test
    @DisplayName("GET /api/users/me/addresses returns the caller's addresses")
    void shouldListAddresses() throws Exception {
        when(userProfileService.listAddresses(1L))
                .thenReturn(java.util.List.of(Address.builder().id(1L).userId(1L).label("Casa").city("SP").state("SP").build()));

        mockMvc.perform(get("/api/users/me/addresses")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].label").value("Casa"));
    }

    @Test
    @DisplayName("POST /api/users/me/addresses creates an address for the caller")
    void shouldCreateAddress() throws Exception {
        when(userProfileService.addAddress(eq(1L), any()))
                .thenReturn(Address.builder().id(1L).userId(1L).label("Casa").city("SP").state("SP").build());

        mockMvc.perform(post("/api/users/me/addresses")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Casa\",\"street\":\"Rua A\",\"city\":\"SP\",\"state\":\"SP\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.label").value("Casa"));
    }

    @Test
    @DisplayName("DELETE /api/users/me/addresses/{id} returns 404 when address is not the caller's")
    void shouldReturn404WhenDeletingMissingAddress() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Endereço não encontrado"))
                .when(userProfileService).deleteAddress(1L, 99L);

        mockMvc.perform(delete("/api/users/me/addresses/99")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/users/me/addresses/{id}/default marks the address as default")
    void shouldSetDefaultAddress() throws Exception {
        when(userProfileService.setDefaultAddress(1L, 5L))
                .thenReturn(Address.builder().id(5L).userId(1L).label("Casa").isDefault(true).city("SP").state("SP").build());

        mockMvc.perform(patch("/api/users/me/addresses/5/default")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }

    @Test
    @DisplayName("GET /api/users/profile without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }
}
