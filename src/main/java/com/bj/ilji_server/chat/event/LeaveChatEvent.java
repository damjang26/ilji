package com.bj.ilji_server.chat.event;

import com.bj.ilji_server.chat.ChatMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeaveChatEvent {

    private final String roomId;
    private final ChatMessage chatMessage;

    public LeaveChatEvent(String roomId, ChatMessage chatMessage) {
        this.roomId = roomId;
        this.chatMessage = chatMessage;
    }
}
