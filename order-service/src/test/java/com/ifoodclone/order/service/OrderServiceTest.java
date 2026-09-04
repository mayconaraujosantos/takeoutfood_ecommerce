package com.ifoodclone.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.ifoodclone.order.client.MenuClient;
import com.ifoodclone.order.client.MenuItemSummary;
import com.ifoodclone.order.client.PaymentClient;
import com.ifoodclone.order.client.PaymentResult;
import com.ifoodclone.order.client.RestaurantClient;
import com.ifoodclone.order.client.RestaurantSummary;
import com.ifoodclone.order.config.GatewayUserContext.UserContext;
import com.ifoodclone.order.dto.OrderDto;
import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.entity.OrderItem;
import com.ifoodclone.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantClient restaurantClient;
    @Mock
    private MenuClient menuClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, restaurantClient, menuClient, paymentClient, kafkaTemplate);
        UserContext.clear();
        UserContext.setUserId(1L);
        UserContext.setUserRoles("CUSTOMER");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("createCart")
    class CreateCartTests {

        @Test
        @DisplayName("Should create a CART order after validating the restaurant")
        void shouldCreateCart() {
            RestaurantSummary restaurant = new RestaurantSummary();
            restaurant.setId(10L);
            restaurant.setActive(true);
            when(restaurantClient.getActiveRestaurant(10L)).thenReturn(restaurant);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.createCart(OrderDto.CreateRequest.builder().restaurantId(10L).build());

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CART);
            assertThat(result.getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItemTests {

        @Test
        @DisplayName("Should add a validated menu item and recalculate the total")
        void shouldAddItem() {
            Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            MenuItemSummary menuItem = new MenuItemSummary();
            menuItem.setId(5L);
            menuItem.setRestaurantId(10L);
            menuItem.setName("Pizza");
            menuItem.setPrice(new BigDecimal("39.90"));
            menuItem.setAvailable(true);
            when(menuClient.getAvailableItem(5L, 10L)).thenReturn(menuItem);

            Order result = orderService.addItem(1L,
                    OrderDto.AddItemRequest.builder().menuItemId(5L).quantity(2).build());

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getTotalAmount()).isEqualByComparingTo("79.80");
        }

        @Test
        @DisplayName("Should reject adding items to another user's cart")
        void shouldRejectForNonOwner() {
            Order order = Order.builder().id(1L).userId(999L).restaurantId(10L).status(OrderStatus.CART).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.addItem(1L,
                    OrderDto.AddItemRequest.builder().menuItemId(5L).quantity(1).build()))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Should reject adding items once the order left CART status")
        void shouldRejectWhenNotInCart() {
            Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CONFIRMED).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.addItem(1L,
                    OrderDto.AddItemRequest.builder().menuItemId(5L).quantity(1).build()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("checkout")
    class CheckoutTests {

        @Test
        @DisplayName("Should move to CONFIRMED when payment is approved")
        void shouldConfirmOnApprovedPayment() {
            Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART)
                    .totalAmount(new BigDecimal("39.90")).build();
            order.getItems().add(OrderItem.builder().id(1L).order(order).menuItemId(5L)
                    .itemName("Pizza").quantity(1).unitPrice(new BigDecimal("39.90")).build());
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult approved = new PaymentResult();
            approved.setStatus("APPROVED");
            when(paymentClient.charge(eq(1L), any(BigDecimal.class), eq("PIX"))).thenReturn(approved);

            Order result = orderService.checkout(1L, OrderDto.CheckoutRequest.builder()
                    .deliveryAddress("Rua Teste, 123")
                    .paymentMethod(Order.PaymentMethod.PIX)
                    .build());

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should move to PAYMENT_FAILED when payment is rejected")
        void shouldFailOnRejectedPayment() {
            Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART)
                    .totalAmount(new BigDecimal("39.90")).build();
            order.getItems().add(OrderItem.builder().id(1L).order(order).menuItemId(5L)
                    .itemName("Pizza").quantity(1).unitPrice(new BigDecimal("39.90")).build());
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult rejected = new PaymentResult();
            rejected.setStatus("REJECTED");
            when(paymentClient.charge(eq(1L), any(BigDecimal.class), eq("PIX"))).thenReturn(rejected);

            Order result = orderService.checkout(1L, OrderDto.CheckoutRequest.builder()
                    .deliveryAddress("Rua Teste, 123")
                    .paymentMethod(Order.PaymentMethod.PIX)
                    .build());

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        }

        @Test
        @DisplayName("Should reject checkout of an empty cart")
        void shouldRejectEmptyCart() {
            Order order = Order.builder().id(1L).userId(1L).restaurantId(10L).status(OrderStatus.CART).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.checkout(1L, OrderDto.CheckoutRequest.builder()
                    .deliveryAddress("Rua Teste, 123")
                    .paymentMethod(Order.PaymentMethod.PIX)
                    .build()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("Should allow the order's owner to view it")
        void shouldAllowOwner() {
            Order order = Order.builder().id(1L).userId(1L).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            Order result = orderService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should reject a different customer viewing someone else's order")
        void shouldRejectOtherCustomer() {
            Order order = Order.builder().id(1L).userId(999L).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.getById(1L)).isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should reject a customer trying to update status")
        void shouldRejectCustomer() {
            assertThatThrownBy(() -> orderService.updateStatus(1L,
                    OrderDto.StatusUpdateRequest.builder().status(OrderStatus.PREPARING).build()))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Should let a restaurant owner move an order to PREPARING")
        void shouldAllowRestaurantOwner() {
            UserContext.setUserRoles("RESTAURANT_OWNER");
            Order order = Order.builder().id(1L).userId(1L).status(OrderStatus.CONFIRMED).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateStatus(1L,
                    OrderDto.StatusUpdateRequest.builder().status(OrderStatus.PREPARING).build());

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("Should reject an out-of-scope status transition")
        void shouldRejectDisallowedTransition() {
            UserContext.setUserRoles("RESTAURANT_OWNER");

            assertThatThrownBy(() -> orderService.updateStatus(1L,
                    OrderDto.StatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).build()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
