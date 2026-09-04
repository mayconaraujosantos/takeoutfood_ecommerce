package com.ifoodclone.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.notification.config.GatewayUserContext.UserContext;
import com.ifoodclone.notification.dto.NotificationDto;
import com.ifoodclone.notification.dto.NotificationDto.ApiResponse;
import com.ifoodclone.notification.dto.NotificationDto.NotificationInfo;
import com.ifoodclone.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Feed de notificações in-app do usuário")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationInfo>>> myNotifications() {
        List<NotificationInfo> notifications = notificationService.getMyNotifications(UserContext.getUserId())
                .stream()
                .map(NotificationInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {
        long count = notificationService.unreadCount(UserContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto.ApiResponse<NotificationInfo>> markAsRead(@PathVariable String id) {
        try {
            var notification = notificationService.markAsRead(id, UserContext.getUserId());
            return ResponseEntity.ok(ApiResponse.success(NotificationInfo.from(notification)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead(UserContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Todas as notificações foram marcadas como lidas", null));
    }
}
