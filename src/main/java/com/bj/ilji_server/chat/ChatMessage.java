package com.bj.ilji_server.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String room;
    private String sender;
    private String message;
    private LocalDateTime timestamp;
}