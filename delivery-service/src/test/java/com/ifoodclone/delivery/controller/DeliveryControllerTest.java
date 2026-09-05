package com.ifoodclone.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.ifoodclone.delivery.config.GatewayUserContext.UserContext;
import com.ifoodclone.delivery.config.TestConfig;
import com.ifoodclone.delivery.entity.Delivery;
import com.ifoodclone.delivery.entity.Delivery.DeliveryStatus;
import com.ifoodclone.delivery.service.DeliveryService;

@WebMvcTest(controllers = DeliveryController.class)
@Import(TestConfig.class)
@DisplayName("Delivery Controller Tests")
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/deliveries/available without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/deliveries/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/deliveries/available rejects a CUSTOMER role")
    void shouldRejectAvailableForWrongRole() throws Exception {
        mockMvc.perform(get("/api/v1/deliveries/available")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/deliveries/available returns the list for a driver")
    void shouldListAvailableForDriver() throws Exception {
        when(deliveryService.listAvailable()).thenReturn(java.util.List.of(
                Delivery.builder().id(1L).orderId(10L).status(DeliveryStatus.AWAITING_DRIVER).build()));

        mockMvc.perform(get("/api/v1/deliveries/available")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "DELIVERY_DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderId").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/deliveries/{id}/accept accepts as a driver")
    void shouldAcceptAsDriver() throws Exception {
        Delivery accepted = Delivery.builder().id(1L).driverId(1L).status(DeliveryStatus.ACCEPTED).build();
        when(deliveryService.accept(1L, 1L)).thenReturn(accepted);

        mockMvc.perform(post("/api/v1/deliveries/1/accept")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "DELIVERY_DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/deliveries/{id}/status returns 403 for unauthorized driver")
    void shouldRejectStatusUpdateFromWrongDriver() throws Exception {
        when(deliveryService.updateStatus(anyLong(), anyLong(), org.mockito.ArgumentMatchers.eq(false), any()))
                .thenThrow(new SecurityException("Você não tem permissão para alterar esta entrega"));

        mockMvc.perform(patch("/api/v1/deliveries/1/status")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "999")
                .header("X-User-Roles", "DELIVERY_DRIVER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PICKED_UP\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/deliveries/{id} returns 404 when not found")
    void shouldReturn404WhenMissing() throws Exception {
        when(deliveryService.getById(99L)).thenThrow(new RuntimeException("Entrega não encontrada"));

        mockMvc.perform(get("/api/v1/deliveries/99")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "DELIVERY_DRIVER"))
                .andExpect(status().isNotFound());
    }
}
