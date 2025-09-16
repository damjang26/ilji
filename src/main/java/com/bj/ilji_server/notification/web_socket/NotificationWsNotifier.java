package com.bj.ilji_server.notification.web_socket;

import com.bj.ilji_server.notification.dto.NotificationDto;
import com.bj.ilji_server.notification.event.NotificationCreatedEvent;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationWsNotifier {

    private final SocketIOServer socketIOServer;

    public NotificationWsNotifier(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent e) {
        NotificationDto notificationDto = NotificationDto.from(e.getNotification());
        // Send notification to the specific user's room
        socketIOServer.getRoomOperations(e.getRecipientId().toString()).sendEvent("notification", notificationDto);
    }
}
