package com.bj.ilji_server.notification.service;

import com.bj.ilji_server.notification.dto.NotificationResponse;
import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.entity.NotificationType;
import com.bj.ilji_server.notification.repository.NotificationRepository;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createFollowNotification(User sender, User recipient) {
        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .notificationType(NotificationType.NEW_FOLLOWER)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to read this notification");
        }

        notification.markAsRead();
    }
}
