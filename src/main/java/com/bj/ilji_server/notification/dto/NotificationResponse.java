package com.bj.ilji_server.notification.dto;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long notificationId;
    private Long senderId;
    private String senderName;
    private NotificationType notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String message;

    public static NotificationResponse from(Notification notification) {
        String message = "";
        if (notification.getNotificationType() == NotificationType.NEW_FOLLOWER) {
            message = notification.getSender().getName() + "님이 회원님을 팔로우하기 시작했습니다.";
        }

        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .senderId(notification.getSender().getId())
                .senderName(notification.getSender().getName())
                .notificationType(notification.getNotificationType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .message(message)
                .build();
    }
}
