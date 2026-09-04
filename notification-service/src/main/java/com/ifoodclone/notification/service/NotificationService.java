package com.ifoodclone.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ifoodclone.notification.entity.Notification;
import com.ifoodclone.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public Notification markAsRead(String id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public Notification create(Long userId, Notification.NotificationType type, String title, String message,
            Long referenceId, String sourceStatus) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .sourceStatus(sourceStatus)
                .read(false)
                .build();
        return notificationRepository.save(notification);
    }
}
