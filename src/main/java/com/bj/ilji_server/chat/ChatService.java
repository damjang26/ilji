package com.bj.ilji_server.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoom> findMyChatRooms(String userId) {
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
    }
}