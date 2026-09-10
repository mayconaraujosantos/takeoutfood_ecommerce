package com.ifoodclone.payment.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

// Talks to the Node-RED payment gateway simulator (see /payment-gateway at the repo root).
// Fire-and-forget: the gateway answers 202 immediately and resolves the charge later via
// POST /api/v1/payments/webhook, same shape a real PSP's async confirmation would have.
@Component
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public PaymentGatewayClient(RestTemplate restTemplate, @Value("${payment.gateway-url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public boolean charge(Long orderId, BigDecimal amount, String method) {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "amount", amount,
                "method", method);

        try {
            restTemplate.postForEntity(gatewayUrl + "/charge", body, Void.class);
            return true;
        } catch (RestClientException ex) {
            return false;
        }
    }
}
