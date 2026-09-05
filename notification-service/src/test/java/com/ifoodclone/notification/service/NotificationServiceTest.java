package com.ifoodclone.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {
        @Test
        @DisplayName("Should persist a notification for the given user")
        void shouldCreateNotification() {
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            Notification result = notificationService.create(1L, Notification.NotificationType.ORDER,
                    "Pedido confirmado!", "msg", 10L, "CONFIRMED");

            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getType()).isEqualTo(Notification.NotificationType.ORDER);
            assertThat(result.isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTests {
        @Test
        @DisplayName("Should mark the owned notification as read")
        void shouldMarkAsRead() {
            Notification notification = Notification.builder().id("n1").userId(1L).read(false).build();
            when(notificationRepository.findByIdAndUserId("n1", 1L)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            Notification result = notificationService.markAsRead("n1", 1L);

            assertThat(result.isRead()).isTrue();
        }

        @Test
        @DisplayName("Should throw when notification doesn't belong to the user")
        void shouldThrowWhenNotOwned() {
            when(notificationRepository.findByIdAndUserId("n1", 999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead("n1", 999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsReadTests {
        @Test
        @DisplayName("Should mark every unread notification as read")
        void shouldMarkAllAsRead() {
            Notification n1 = Notification.builder().id("n1").userId(1L).read(false).build();
            Notification n2 = Notification.builder().id("n2").userId(1L).read(false).build();
            when(notificationRepository.findByUserIdAndReadFalse(1L)).thenReturn(List.of(n1, n2));

            notificationService.markAllAsRead(1L);

            assertThat(n1.isRead()).isTrue();
            assertThat(n2.isRead()).isTrue();
        }
    }
}
