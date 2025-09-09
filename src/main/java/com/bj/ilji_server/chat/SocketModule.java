package com.bj.ilji_server.chat;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketModule {

    private final SocketIOServer server;

    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @PostConstruct
    private void init() {
        server.addConnectListener(client -> {
            log.info("New client connected: {}", client.getSessionId());
        });

        server.addDisconnectListener(client -> {
            log.info("Client disconnected: {}", client.getSessionId());
        });

        // 'joinRoom' 이벤트를 받을 리스너
        server.addEventListener("joinRoom", String.class, (client, room, ackSender) -> {
            log.info("Client [{}] joined room: {}", client.getSessionId(), room);
            client.joinRoom(room);
        });

        // 'chatMessage' 이벤트를 받을 리스너
        server.addEventListener("chatMessage", ChatMessage.class, (client, data, ackSender) -> {

            String jsonLog = objectMapper.writeValueAsString(data);
            // MongoDB 저장
            chatMessageRepository.save(data);
            log.info("Message from client [{}]: {}", client.getSessionId(), data.getMessage());


            // 받은 메시지를 해당 방의 모든 클라이언트에게 다시 보냅니다.
            // 'sendEvent'의 첫 번째 인자는 클라이언트에서 받을 이벤트 이름입니다.
            server.getRoomOperations(data.getRoomId()).sendEvent("chatMessage", data);
        });
    }
}