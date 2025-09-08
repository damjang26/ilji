package com.bj.ilji_server.chat;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    private String roomId;
    private String sender;
    private String receiver;
    private String message;
    private LocalDateTime timestamp;
}