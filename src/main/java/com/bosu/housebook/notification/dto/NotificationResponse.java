package com.bosu.housebook.notification.dto;

import com.bosu.housebook.notification.Notification;
import com.bosu.housebook.notification.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        String link,
        boolean read,
        LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getBody(), notification.getLink(), notification.isRead(), notification.getCreatedAt());
    }
}
