package com.ifoodclone.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.order.dto.OrderDto;
import com.ifoodclone.order.dto.OrderDto.ApiResponse;
import com.ifoodclone.order.dto.OrderDto.OrderInfo;
import com.ifoodclone.order.entity.Order;
import com.ifoodclone.order.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Carrinho, checkout e acompanhamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderInfo>> createCart(@Valid @RequestBody OrderDto.CreateRequest request) {
        try {
            Order order = orderService.createCart(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Carrinho criado", OrderInfo.from(order)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderInfo>> addItem(@PathVariable Long id,
            @Valid @RequestBody OrderDto.AddItemRequest request) {
        return handle(() -> orderService.addItem(id, request), "Item adicionado ao carrinho");
    }

    @PatchMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderInfo>> updateItem(@PathVariable Long id, @PathVariable Long itemId,
            @Valid @RequestBody OrderDto.UpdateItemRequest request) {
        return handle(() -> orderService.updateItem(id, itemId, request), "Item atualizado");
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderInfo>> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        return handle(() -> orderService.removeItem(id, itemId), "Item removido");
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<ApiResponse<OrderInfo>> checkout(@PathVariable Long id,
            @Valid @RequestBody OrderDto.CheckoutRequest request) {
        return handle(() -> orderService.checkout(id, request), "Checkout concluído");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderInfo>> getById(@PathVariable Long id) {
        return handle(() -> orderService.getById(id), null);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderInfo>>> getMyOrders() {
        List<OrderInfo> orders = orderService.getMyOrders().stream().map(OrderInfo::from).toList();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderInfo>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody OrderDto.StatusUpdateRequest request) {
        return handle(() -> orderService.updateStatus(id, request), "Status do pedido atualizado");
    }

    private ResponseEntity<ApiResponse<OrderInfo>> handle(java.util.function.Supplier<Order> action, String successMessage) {
        try {
            Order order = action.get();
            OrderInfo info = OrderInfo.from(order);
            return ResponseEntity.ok(successMessage != null ? ApiResponse.success(successMessage, info) : ApiResponse.success(info));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
