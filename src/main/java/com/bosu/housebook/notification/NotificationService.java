package com.bosu.housebook.notification;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.notification.dto.NotificationResponse;
import com.bosu.housebook.push.ExpoPushSender;
import com.bosu.housebook.push.PushToken;
import com.bosu.housebook.push.PushTokenRepository;
import com.bosu.housebook.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushSender expoPushSender;

    public NotificationService(NotificationRepository notificationRepository,
            PushTokenRepository pushTokenRepository, ExpoPushSender expoPushSender) {
        this.notificationRepository = notificationRepository;
        this.pushTokenRepository = pushTokenRepository;
        this.expoPushSender = expoPushSender;
    }

    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findTop50ByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("알림을 찾을 수 없습니다."));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw ApiException.forbidden("본인의 알림만 처리할 수 있습니다.");
        }
        notification.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByRecipientIdAndReadFalse(userId).forEach(Notification::markRead);
    }

    @Transactional
    public void create(User recipient, NotificationType type, String title, String body, String link) {
        notificationRepository.save(new Notification(recipient, type, title, body, link));
        List<String> tokens = pushTokenRepository.findByUserId(recipient.getId()).stream()
                .map(PushToken::getToken)
                .toList();
        expoPushSender.send(tokens, title, body, link);
    }
}
