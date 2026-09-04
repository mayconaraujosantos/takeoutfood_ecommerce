package com.ifoodclone.notification.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.ifoodclone.notification.config.GatewayUserContext.UserContext;
import com.ifoodclone.notification.config.TestConfig;
import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.service.NotificationService;

@WebMvcTest(controllers = NotificationController.class)
@Import(TestConfig.class)
@DisplayName("Notification Controller Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/notifications without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/notifications returns the list from the service")
    void shouldListNotifications() throws Exception {
        Notification notification = Notification.builder()
                .id("n1").userId(1L).type(Notification.NotificationType.ORDER)
                .title("Pedido confirmado!").message("msg").referenceId(10L).read(false).build();
        when(notificationService.getMyNotifications(1L)).thenReturn(java.util.List.of(notification));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-Authenticated", "true")
                        .header("X-User-Id", "1")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Pedido confirmado!"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count returns the count")
    void shouldReturnUnreadCount() throws Exception {
        when(notificationService.unreadCount(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("X-Authenticated", "true")
                        .header("X-User-Id", "1")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read returns 404 when not found")
    void shouldReturn404WhenMarkingMissingNotification() throws Exception {
        when(notificationService.markAsRead("missing", 1L))
                .thenThrow(new RuntimeException("Notificação não encontrada"));

        mockMvc.perform(patch("/api/v1/notifications/missing/read")
                        .header("X-Authenticated", "true")
                        .header("X-User-Id", "1")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/read-all marks everything as read")
    void shouldMarkAllAsRead() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("X-Authenticated", "true")
                        .header("X-User-Id", "1")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        org.mockito.Mockito.verify(notificationService).markAllAsRead(anyLong());
    }
}
