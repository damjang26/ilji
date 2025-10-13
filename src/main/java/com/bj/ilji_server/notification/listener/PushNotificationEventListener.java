package com.bj.ilji_server.notification.listener;

import com.bj.ilji_server.notification.event.NotificationCreatedEvent;
import com.bj.ilji_server.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final PushNotificationService pushNotificationService;

    @Async
    @EventListener
    public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
        pushNotificationService.sendPushNotification(event.getNotification());
    }
}
