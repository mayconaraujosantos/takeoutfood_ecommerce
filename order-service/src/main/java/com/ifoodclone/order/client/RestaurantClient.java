package com.ifoodclone.order.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestaurantClient {

    private static final String BASE_URL = "http://restaurant-service/api/v1/restaurants";

    private final RestTemplate restTemplate;

    public RestaurantClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestaurantSummary getActiveRestaurant(Long restaurantId) {
        try {
            ResponseEntity<ExternalApiResponse<RestaurantSummary>> response = restTemplate.exchange(
                    BASE_URL + "/" + restaurantId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ExternalApiResponse<RestaurantSummary>>() {
                    });

            RestaurantSummary restaurant = response.getBody() != null ? response.getBody().getData() : null;
            if (restaurant == null || !Boolean.TRUE.equals(restaurant.getActive())) {
                throw new RuntimeException("Restaurante não encontrado ou inativo");
            }
            return restaurant;
        } catch (RestClientException ex) {
            throw new RuntimeException("Não foi possível validar o restaurante: " + ex.getMessage());
        }
    }
}
