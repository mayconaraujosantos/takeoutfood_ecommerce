package com.ifoodclone.notification.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private NotificationType type;

    private String title;

    private String message;

    // orderId or deliveryId depending on type -- no shared module between services,
    // so this stores whatever id the source Kafka event carried.
    private Long referenceId;

    // Raw status string from the source event (e.g. "CONFIRMED", "DELIVERED"),
    // kept for traceability/debugging even though title/message already translate it.
    private String sourceStatus;

    @Builder.Default
    private boolean read = false;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum NotificationType {
        ORDER,
        DELIVERY
    }
}
