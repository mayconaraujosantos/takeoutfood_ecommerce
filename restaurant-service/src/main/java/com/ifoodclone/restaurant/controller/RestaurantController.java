package com.ifoodclone.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.restaurant.config.GatewayUserContext.UserContext;
import com.ifoodclone.restaurant.dto.RestaurantDto;
import com.ifoodclone.restaurant.dto.RestaurantDto.ApiResponse;
import com.ifoodclone.restaurant.dto.RestaurantDto.RestaurantInfo;
import com.ifoodclone.restaurant.entity.Restaurant;
import com.ifoodclone.restaurant.service.RestaurantService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Cadastro e busca de restaurantes")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantInfo>> create(@Valid @RequestBody RestaurantDto.CreateRequest request) {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas donos de restaurante podem cadastrar um restaurante"));
        }

        try {
            Restaurant restaurant = restaurantService.create(request, UserContext.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurante criado com sucesso", RestaurantInfo.from(restaurant)));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha ao criar restaurante", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantInfo>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cuisineType) {
        List<RestaurantInfo> restaurants = restaurantService.search(name, cuisineType).stream()
                .map(RestaurantInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantInfo>> getById(@PathVariable Long id) {
        try {
            Restaurant restaurant = restaurantService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(RestaurantInfo.from(restaurant)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantInfo>> update(@PathVariable Long id,
            @RequestBody RestaurantDto.UpdateRequest request) {
        try {
            Restaurant restaurant = restaurantService.update(id, request);
            return ResponseEntity.ok(ApiResponse.success("Restaurante atualizado com sucesso", RestaurantInfo.from(restaurant)));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            restaurantService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Restaurante removido com sucesso", null));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
