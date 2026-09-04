package com.ifoodclone.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.menu.dto.MenuDto;
import com.ifoodclone.menu.entity.MenuItem;
import com.ifoodclone.menu.repository.MenuItemRepository;

@Service
@Transactional
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public MenuItem create(MenuDto.CreateRequest request) {
        MenuItem item = MenuItem.builder()
                .restaurantId(request.getRestaurantId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .available(true)
                .build();

        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    @Transactional(readOnly = true)
    public MenuItem getById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de cardápio não encontrado"));
    }

    public MenuItem update(Long id, MenuDto.UpdateRequest request) {
        MenuItem item = getById(id);

        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }

        return menuItemRepository.save(item);
    }

    public void delete(Long id) {
        MenuItem item = getById(id);
        menuItemRepository.delete(item);
    }
}
