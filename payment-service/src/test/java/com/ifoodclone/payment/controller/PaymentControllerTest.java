package com.ifoodclone.payment.controller;

import static org.mockito.ArgumentMatchers.any;
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

import com.ifoodclone.payment.config.GatewayUserContext.UserContext;
import com.ifoodclone.payment.config.TestConfig;
import com.ifoodclone.payment.entity.Payment;
import com.ifoodclone.payment.service.PaymentService;

@WebMvcTest(controllers = PaymentController.class)
@Import(TestConfig.class)
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/payments returns 201 when approved")
    void shouldReturn201WhenApproved() throws Exception {
        Payment approved = Payment.builder().id(1L).orderId(10L).amount(new BigDecimal("59.90"))
                .method(Payment.PaymentMethod.PIX).status(Payment.PaymentStatus.APPROVED).build();
        when(paymentService.process(any())).thenReturn(approved);

        mockMvc.perform(post("/api/v1/payments")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":10,\"amount\":59.90,\"method\":\"PIX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments returns 402 when rejected")
    void shouldReturn402WhenRejected() throws Exception {
        Payment rejected = Payment.builder().id(1L).orderId(10L).amount(BigDecimal.ZERO)
                .method(Payment.PaymentMethod.PIX).status(Payment.PaymentStatus.REJECTED).build();
        when(paymentService.process(any())).thenReturn(rejected);

        mockMvc.perform(post("/api/v1/payments")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":10,\"amount\":59.90,\"method\":\"PIX\"}"))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    @DisplayName("GET /api/v1/payments/order/{orderId} returns 404 when missing")
    void shouldReturn404WhenMissing() throws Exception {
        when(paymentService.getByOrderId(99L)).thenThrow(new RuntimeException("Pagamento não encontrado para este pedido"));

        mockMvc.perform(get("/api/v1/payments/order/99")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/payments without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":10,\"amount\":59.90,\"method\":\"PIX\"}"))
                .andExpect(status().isUnauthorized());
    }
}
