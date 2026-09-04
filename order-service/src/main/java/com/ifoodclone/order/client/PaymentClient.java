package com.ifoodclone.order.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    private static final String BASE_URL = "http://payment-service/api/v1/payments";

    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResult charge(Long orderId, BigDecimal amount, String paymentMethod) {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "amount", amount,
                "method", paymentMethod);

        try {
            ResponseEntity<ExternalApiResponse<PaymentResult>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<ExternalApiResponse<PaymentResult>>() {
                    });

            ExternalApiResponse<PaymentResult> apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData();
            }

            // payment-service returns 402 (still a valid JSON body) when it rejects the
            // charge -- RestTemplate only throws on that for its default error handler,
            // so a rejected payment surfaces as a RestClientException below, not here.
            PaymentResult rejected = new PaymentResult();
            rejected.setOrderId(orderId);
            rejected.setStatus("REJECTED");
            return rejected;
        } catch (RestClientException ex) {
            PaymentResult rejected = new PaymentResult();
            rejected.setOrderId(orderId);
            rejected.setStatus("REJECTED");
            return rejected;
        }
    }
}
