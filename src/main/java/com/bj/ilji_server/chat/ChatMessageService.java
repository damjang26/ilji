package com.bj.ilji_server.chat;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
}
