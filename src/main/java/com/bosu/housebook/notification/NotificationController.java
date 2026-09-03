package com.bosu.housebook.notification;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.notification.dto.NotificationResponse;
import com.bosu.housebook.notification.dto.UnreadCountResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAll(@CurrentUserId Long userId) {
        return notificationService.getForUser(userId);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(@CurrentUserId Long userId) {
        return new UnreadCountResponse(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/{notificationId}/read")
    public void markRead(@CurrentUserId Long userId, @PathVariable Long notificationId) {
        notificationService.markRead(userId, notificationId);
    }

    @PostMapping("/read-all")
    public void markAllRead(@CurrentUserId Long userId) {
        notificationService.markAllRead(userId);
    }
}
