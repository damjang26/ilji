package com.bj.ilji_server.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    // user1Id 또는 user2Id가 주어진 userId와 일치하는 모든 채팅방을 찾습니다.
    List<ChatRoom> findByUser1IdOrUser2Id(String user1Id, String user2Id);
}