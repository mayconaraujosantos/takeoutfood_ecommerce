package com.ifoodclone.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.restaurant.config.GatewayUserContext.UserContext;
import com.ifoodclone.restaurant.dto.RestaurantDto;
import com.ifoodclone.restaurant.entity.Restaurant;
import com.ifoodclone.restaurant.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Service Tests")
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(restaurantRepository);
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Should create restaurant owned by the requesting user")
        void shouldCreateRestaurant() {
            RestaurantDto.CreateRequest request = RestaurantDto.CreateRequest.builder()
                    .name("Pizza Place")
                    .cuisineType("Italian")
                    .build();

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Restaurant result = restaurantService.create(request, 42L);

            assertThat(result.getName()).isEqualTo("Pizza Place");
            assertThat(result.getOwnerId()).isEqualTo(42L);
            assertThat(result.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("Should return the restaurant when found and active")
        void shouldReturnRestaurant() {
            Restaurant restaurant = Restaurant.builder().id(1L).name("Sushi Bar").build();
            when(restaurantRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(restaurant));

            Restaurant result = restaurantService.getById(1L);

            assertThat(result.getName()).isEqualTo("Sushi Bar");
        }

        @Test
        @DisplayName("Should throw when restaurant is missing or inactive")
        void shouldThrowWhenMissing() {
            when(restaurantRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> restaurantService.getById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("Should update when requester is the owner")
        void shouldUpdateAsOwner() {
            Restaurant restaurant = Restaurant.builder().id(1L).name("Old Name").ownerId(42L).active(true).build();
            when(restaurantRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

            UserContext.setUserId(42L);
            UserContext.setUserRoles("RESTAURANT_OWNER");

            Restaurant result = restaurantService
                    .update(1L, RestaurantDto.UpdateRequest.builder().name("New Name").build());

            assertThat(result.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Should reject update from a different, non-admin user")
        void shouldRejectUpdateFromNonOwner() {
            Restaurant restaurant = Restaurant.builder().id(1L).name("Old Name").ownerId(42L).active(true).build();
            when(restaurantRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(restaurant));

            UserContext.setUserId(999L);
            UserContext.setUserRoles("CUSTOMER");

            assertThatThrownBy(() -> restaurantService
                    .update(1L, RestaurantDto.UpdateRequest.builder().name("Hijacked").build()))
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should soft-delete by setting active to false")
        void shouldSoftDelete() {
            Restaurant restaurant = Restaurant.builder().id(1L).ownerId(42L).active(true).build();
            when(restaurantRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

            UserContext.setUserId(42L);

            restaurantService.delete(1L);

            verify(restaurantRepository).save(restaurant);
            assertThat(restaurant.getActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("search")
    class SearchTests {

        @Test
        @DisplayName("Should delegate to repository search")
        void shouldDelegateSearch() {
            when(restaurantRepository.search("Pizza", null)).thenReturn(List.of(Restaurant.builder().id(1L).build()));

            List<Restaurant> result = restaurantService.search("Pizza", null);

            assertThat(result).hasSize(1);
        }
    }
}
