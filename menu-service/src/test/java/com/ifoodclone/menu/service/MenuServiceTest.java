package com.ifoodclone.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.menu.dto.MenuDto;
import com.ifoodclone.menu.entity.MenuItem;
import com.ifoodclone.menu.repository.MenuItemRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Menu Service Tests")
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(menuItemRepository);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Should create an available menu item for the given restaurant")
        void shouldCreateMenuItem() {
            MenuDto.CreateRequest request = MenuDto.CreateRequest.builder()
                    .restaurantId(10L)
                    .name("Margherita")
                    .price(new BigDecimal("39.90"))
                    .build();

            when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

            MenuItem result = menuService.create(request);

            assertThat(result.getRestaurantId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Margherita");
            assertThat(result.getAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("getByRestaurant")
    class GetByRestaurantTests {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegate() {
            when(menuItemRepository.findByRestaurantIdAndAvailableTrue(10L))
                    .thenReturn(List.of(MenuItem.builder().id(1L).restaurantId(10L).build()));

            List<MenuItem> result = menuService.getByRestaurant(10L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("Should throw when item is missing")
        void shouldThrowWhenMissing() {
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.getById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("Should apply only the provided fields")
        void shouldPartiallyUpdate() {
            MenuItem item = MenuItem.builder().id(1L).name("Old").price(new BigDecimal("10.00")).available(true).build();
            when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

            MenuItem result = menuService.update(1L, MenuDto.UpdateRequest.builder().name("New").build());

            assertThat(result.getName()).isEqualTo("New");
            assertThat(result.getPrice()).isEqualByComparingTo("10.00");
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should delete the item")
        void shouldDelete() {
            MenuItem item = MenuItem.builder().id(1L).build();
            when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

            menuService.delete(1L);

            verify(menuItemRepository).delete(item);
        }
    }
}
