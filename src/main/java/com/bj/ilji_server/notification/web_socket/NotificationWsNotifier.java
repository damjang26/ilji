package com.bj.ilji_server.notification.web_socket;

import com.bj.ilji_server.notification.dto.NotificationDto;
import com.bj.ilji_server.notification.event.NotificationCreatedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationWsNotifier {

    private final SimpMessagingTemplate template;

    public NotificationWsNotifier(SimpMessagingTemplate template) {
        this.template = template;
    }

    private String topic(Long recipientId) {
        // 프론트 구독 경로: /topic/notifications/{userId}
        return "/topic/notifications/" + recipientId;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent e) {
        // ⚠️ 래핑 없이 DTO를 그대로 보냅니다 (합의한 형태)
        template.convertAndSend(topic(e.getRecipientId()), NotificationDto.from(e.getNotification()));
    }
}
