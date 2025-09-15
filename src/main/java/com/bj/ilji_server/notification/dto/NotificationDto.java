package com.bj.ilji_server.notification.dto;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.type.EntityType;
import com.bj.ilji_server.notification.type.NotificationStatus;
import com.bj.ilji_server.notification.type.NotificationType;

import java.time.OffsetDateTime;

public record NotificationDto(
        Long id,
        Long recipientId,
        Long senderId,
        NotificationType type,
        EntityType entityType,
        Long entityId,
        String messageTitle,
        String messageBody,
        String linkUrl,
        NotificationStatus status,
        OffsetDateTime createdAt,
        String metaJson
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getRecipientId(),
                n.getSenderId(),
                n.getType(),
                n.getEntityType(),
                n.getEntityId(),
                n.getMessageTitle(),
                n.getMessageBody(),
                n.getLinkUrl(),
                n.getStatus(),
                n.getCreatedAt(),
                n.getMetaJson()
        );
    }
}
