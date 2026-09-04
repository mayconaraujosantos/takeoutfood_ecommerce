package com.ifoodclone.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

import com.ifoodclone.order.config.GatewayUserContext.UserContext;
import com.ifoodclone.order.config.TestConfig;
import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.service.OrderService;

@WebMvcTest(controllers = OrderController.class)
@Import(TestConfig.class)
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/orders creates a cart")
    void shouldCreateCart() throws Exception {
        Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART).build();
        when(orderService.createCart(any())).thenReturn(order);

        mockMvc.perform(post("/api/v1/orders")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CART"));
    }

    @Test
    @DisplayName("POST /api/v1/orders without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":10}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/orders/me returns the user's orders")
    void shouldListMyOrders() throws Exception {
        when(orderService.getMyOrders()).thenReturn(java.util.List.of(
                Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART).build()));

        mockMvc.perform(get("/api/v1/orders/me")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} returns 403 for someone else's order")
    void shouldReturn403ForForbidden() throws Exception {
        when(orderService.getById(1L)).thenThrow(new SecurityException("Você não tem permissão para ver este pedido"));

        mockMvc.perform(get("/api/v1/orders/1")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "999")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
