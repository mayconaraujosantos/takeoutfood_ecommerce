package com.ifoodclone.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.restaurant.config.GatewayUserContext.UserContext;
import com.ifoodclone.restaurant.dto.RestaurantDto;
import com.ifoodclone.restaurant.entity.Restaurant;
import com.ifoodclone.restaurant.repository.RestaurantRepository;

@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public Restaurant create(RestaurantDto.CreateRequest request, Long ownerId) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cuisineType(request.getCuisineType())
                .address(request.getAddress())
                .phone(request.getPhone())
                .ownerId(ownerId)
                .active(true)
                .build();

        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public List<Restaurant> search(String name, String cuisineType) {
        return restaurantRepository.search(name, cuisineType);
    }

    @Transactional(readOnly = true)
    public Restaurant getById(Long id) {
        return restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
    }

    public Restaurant update(Long id, RestaurantDto.UpdateRequest request) {
        Restaurant restaurant = getById(id);
        assertOwnerOrAdmin(restaurant);

        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getCuisineType() != null) {
            restaurant.setCuisineType(request.getCuisineType());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(request.getPhone());
        }

        return restaurantRepository.save(restaurant);
    }

    public void delete(Long id) {
        Restaurant restaurant = getById(id);
        assertOwnerOrAdmin(restaurant);
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    private void assertOwnerOrAdmin(Restaurant restaurant) {
        boolean isOwner = restaurant.getOwnerId().equals(UserContext.getUserId());
        if (!isOwner && !UserContext.isAdmin()) {
            throw new SecurityException("Você não tem permissão para alterar este restaurante");
        }
    }
}
