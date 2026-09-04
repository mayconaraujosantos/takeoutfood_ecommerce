package com.ifoodclone.menu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ifoodclone.menu.config.GatewayUserContext.UserContext;
import com.ifoodclone.menu.config.TestConfig;
import com.ifoodclone.menu.entity.MenuItem;
import com.ifoodclone.menu.service.MenuService;

@WebMvcTest(controllers = MenuController.class)
@Import(TestConfig.class)
@DisplayName("Menu Controller Tests")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/menus without RESTAURANT_OWNER/ADMIN role returns 403")
    void shouldRejectCreateForWrongRole() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":10,\"name\":\"Pizza\",\"price\":39.90}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/menus as RESTAURANT_OWNER returns 201")
    void shouldCreateAsOwner() throws Exception {
        MenuItem saved = MenuItem.builder().id(1L).restaurantId(10L).name("Pizza")
                .price(new BigDecimal("39.90")).available(true).build();
        when(menuService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/menus")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "RESTAURANT_OWNER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":10,\"name\":\"Pizza\",\"price\":39.90}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Pizza"));
    }

    @Test
    @DisplayName("GET /api/v1/menus/restaurant/{id} returns the restaurant's menu")
    void shouldListMenuForRestaurant() throws Exception {
        when(menuService.getByRestaurant(eq(10L)))
                .thenReturn(java.util.List.of(MenuItem.builder().id(1L).restaurantId(10L).name("Pizza").build()));

        mockMvc.perform(get("/api/v1/menus/restaurant/10")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Pizza"));
    }

    @Test
    @DisplayName("GET /api/v1/menus/{id} returns 404 when missing")
    void shouldReturn404WhenMissing() throws Exception {
        when(menuService.getById(99L)).thenThrow(new RuntimeException("Item de cardápio não encontrado"));

        mockMvc.perform(get("/api/v1/menus/99")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }
}
