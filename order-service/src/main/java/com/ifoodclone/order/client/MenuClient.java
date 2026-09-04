package com.ifoodclone.order.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MenuClient {

    private static final String BASE_URL = "http://menu-service/api/v1/menus";

    private final RestTemplate restTemplate;

    public MenuClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MenuItemSummary getAvailableItem(Long menuItemId, Long restaurantId) {
        try {
            ResponseEntity<ExternalApiResponse<MenuItemSummary>> response = restTemplate.exchange(
                    BASE_URL + "/" + menuItemId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ExternalApiResponse<MenuItemSummary>>() {
                    });

            MenuItemSummary item = response.getBody() != null ? response.getBody().getData() : null;
            if (item == null || !Boolean.TRUE.equals(item.getAvailable())) {
                throw new RuntimeException("Item de cardápio não encontrado ou indisponível");
            }
            if (!item.getRestaurantId().equals(restaurantId)) {
                throw new RuntimeException("Item de cardápio não pertence a este restaurante");
            }
            return item;
        } catch (RestClientException ex) {
            throw new RuntimeException("Não foi possível validar o item de cardápio: " + ex.getMessage());
        }
    }
}
