package com.ifoodclone.menu.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.menu.config.GatewayUserContext.UserContext;
import com.ifoodclone.menu.dto.MenuDto;
import com.ifoodclone.menu.dto.MenuDto.ApiResponse;
import com.ifoodclone.menu.dto.MenuDto.MenuItemInfo;
import com.ifoodclone.menu.entity.MenuItem;
import com.ifoodclone.menu.service.MenuService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/menus")
@Tag(name = "Menu", description = "Itens de cardápio dos restaurantes")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemInfo>> create(@Valid @RequestBody MenuDto.CreateRequest request) {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas donos de restaurante podem cadastrar itens de cardápio"));
        }

        try {
            MenuItem item = menuService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Item de cardápio criado com sucesso", MenuItemInfo.from(item)));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha ao criar item de cardápio", ex.getMessage()));
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemInfo>>> getByRestaurant(@PathVariable Long restaurantId) {
        List<MenuItemInfo> items = menuService.getByRestaurant(restaurantId).stream()
                .map(MenuItemInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemInfo>> getById(@PathVariable Long id) {
        try {
            MenuItem item = menuService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(MenuItemInfo.from(item)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemInfo>> update(@PathVariable Long id,
            @RequestBody MenuDto.UpdateRequest request) {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas donos de restaurante podem editar itens de cardápio"));
        }

        try {
            MenuItem item = menuService.update(id, request);
            return ResponseEntity.ok(ApiResponse.success("Item de cardápio atualizado com sucesso", MenuItemInfo.from(item)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Apenas donos de restaurante podem remover itens de cardápio"));
        }

        try {
            menuService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Item de cardápio removido com sucesso", null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
