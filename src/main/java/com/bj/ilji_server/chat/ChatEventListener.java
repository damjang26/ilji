package com.bj.ilji_server.chat;

import com.bj.ilji_server.chat.event.LeaveChatEvent;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final SocketIOServer socketIOServer;

    @TransactionalEventListener
    public void handleLeaveChatEvent(LeaveChatEvent event) {
        socketIOServer.getRoomOperations(event.getRoomId()).sendEvent("chatMessage", event.getChatMessage());
    }
}
