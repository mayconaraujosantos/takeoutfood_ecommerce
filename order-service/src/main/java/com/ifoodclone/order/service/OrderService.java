package com.ifoodclone.order.service;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.order.client.MenuClient;
import com.ifoodclone.order.client.MenuItemSummary;
import com.ifoodclone.order.client.PaymentClient;
import com.ifoodclone.order.client.PaymentResult;
import com.ifoodclone.order.client.RestaurantClient;
import com.ifoodclone.order.config.GatewayUserContext.UserContext;
import com.ifoodclone.order.dto.OrderDto;
import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.entity.Order.OrderStatus;
import com.ifoodclone.order.entity.OrderItem;
import com.ifoodclone.order.event.OrderStatusChangedEvent;
import com.ifoodclone.order.repository.OrderRepository;

@Service
@Transactional
public class OrderService {

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final MenuClient menuClient;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, RestaurantClient restaurantClient,
            MenuClient menuClient, PaymentClient paymentClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.restaurantClient = restaurantClient;
        this.menuClient = menuClient;
        this.paymentClient = paymentClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order createCart(OrderDto.CreateRequest request) {
        restaurantClient.getActiveRestaurant(request.getRestaurantId());

        Order order = Order.builder()
                .userId(UserContext.getUserId())
                .restaurantId(request.getRestaurantId())
                .status(OrderStatus.CART)
                .build();

        return orderRepository.save(order);
    }

    public Order addItem(Long orderId, OrderDto.AddItemRequest request) {
        Order order = getOwnedCart(orderId);

        MenuItemSummary menuItem = menuClient.getAvailableItem(request.getMenuItemId(), order.getRestaurantId());

        order.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                        () -> order.getItems().add(OrderItem.builder()
                                .order(order)
                                .menuItemId(menuItem.getId())
                                .itemName(menuItem.getName())
                                .quantity(request.getQuantity())
                                .unitPrice(menuItem.getPrice())
                                .notes(request.getNotes())
                                .build()));

        order.recalculateTotal();
        return orderRepository.save(order);
    }

    public Order updateItem(Long orderId, Long itemId, OrderDto.UpdateItemRequest request) {
        Order order = getOwnedCart(orderId);
        OrderItem item = findItem(order, itemId);

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }

        order.recalculateTotal();
        return orderRepository.save(order);
    }

    public Order removeItem(Long orderId, Long itemId) {
        Order order = getOwnedCart(orderId);
        OrderItem item = findItem(order, itemId);

        order.getItems().remove(item);
        order.recalculateTotal();
        return orderRepository.save(order);
    }

    public Order checkout(Long orderId, OrderDto.CheckoutRequest request) {
        Order order = getOwnedCart(orderId);

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar um pedido sem itens");
        }

        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        publishStatusChanged(order);

        PaymentResult payment = paymentClient.charge(order.getId(), order.getTotalAmount(),
                request.getPaymentMethod().name());

        order.setStatus("APPROVED".equals(payment.getStatus()) ? OrderStatus.CONFIRMED : OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        publishStatusChanged(order);

        return order;
    }

    @Transactional(readOnly = true)
    public Order getById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        assertCanView(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders() {
        return orderRepository.findByUserId(UserContext.getUserId());
    }

    public Order updateStatus(Long orderId, OrderDto.StatusUpdateRequest request) {
        if (!UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            throw new SecurityException("Apenas o restaurante pode atualizar o status do pedido");
        }
        if (request.getStatus() != OrderStatus.PREPARING && request.getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Transição de status não permitida por este endpoint");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        order.setStatus(request.getStatus());
        orderRepository.save(order);
        publishStatusChanged(order);

        return order;
    }

    private Order getOwnedCart(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (!order.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new SecurityException("Este pedido não pertence a você");
        }
        if (order.getStatus() != OrderStatus.CART) {
            throw new IllegalStateException("Só é possível alterar itens enquanto o pedido está no carrinho");
        }
        return order;
    }

    private OrderItem findItem(Order order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado neste pedido"));
    }

    // Restaurant-side visibility isn't scoped to "only the order's own restaurant"
    // here -- that would need another remote call to restaurant-service to resolve
    // ownerId for order.restaurantId. Any RESTAURANT_OWNER/ADMIN can view any order,
    // a known simplification consistent with menu-service's authorization scope.
    private void assertCanView(Order order) {
        boolean isOwner = order.getUserId().equals(UserContext.getUserId());
        if (!isOwner && !UserContext.isRestaurantOwner() && !UserContext.isAdmin()) {
            throw new SecurityException("Você não tem permissão para ver este pedido");
        }
    }

    private void publishStatusChanged(Order order) {
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getId().toString(), OrderStatusChangedEvent.from(order));
    }
}
