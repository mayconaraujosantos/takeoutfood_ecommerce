package com.ifoodclone.restaurant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.ifoodclone.restaurant.config.GatewayUserContext.UserContext;
import com.ifoodclone.restaurant.config.TestConfig;
import com.ifoodclone.restaurant.entity.Restaurant;
import com.ifoodclone.restaurant.service.RestaurantService;

@WebMvcTest(controllers = RestaurantController.class)
@Import(TestConfig.class)
@DisplayName("Restaurant Controller Tests")
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/restaurants without RESTAURANT_OWNER/ADMIN role returns 403")
    void shouldRejectCreateForWrongRole() throws Exception {
        mockMvc.perform(post("/api/v1/restaurants")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"cuisineType\":\"Italian\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/restaurants as RESTAURANT_OWNER returns 201")
    void shouldCreateAsOwner() throws Exception {
        Restaurant saved = Restaurant.builder().id(1L).name("Test").cuisineType("Italian").ownerId(1L).active(true).build();
        when(restaurantService.create(any(), anyLong())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/restaurants")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "RESTAURANT_OWNER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"cuisineType\":\"Italian\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test"));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/restaurants returns the list from the service")
    void shouldListRestaurants() throws Exception {
        when(restaurantService.search(eq(null), eq(null)))
                .thenReturn(java.util.List.of(Restaurant.builder().id(1L).name("Test").active(true).build()));

        mockMvc.perform(get("/api/v1/restaurants")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test"));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants/{id} returns 404 when not found")
    void shouldReturn404WhenMissing() throws Exception {
        when(restaurantService.getById(99L)).thenThrow(new RuntimeException("Restaurante não encontrado"));

        mockMvc.perform(get("/api/v1/restaurants/99")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/restaurants/{id} returns 403 when requester is not the owner")
    void shouldRejectDeleteFromNonOwner() throws Exception {
        org.mockito.Mockito.doThrow(new SecurityException("Você não tem permissão para alterar este restaurante"))
                .when(restaurantService).delete(1L);

        mockMvc.perform(delete("/api/v1/restaurants/1")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "999")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
