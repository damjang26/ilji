package com.bj.ilji_server.notification.event;

import com.bj.ilji_server.notification.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationCreatedEvent {
    private final Long recipientId;
    private final Notification notification;

    public Long getRecipientId() {
        return recipientId;
    }

    public Notification getNotification() {
        return notification;
    }
}
