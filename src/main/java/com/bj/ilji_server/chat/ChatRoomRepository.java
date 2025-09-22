package com.bj.ilji_server.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    // JPQL 쿼리를 사용하여 특정 사용자가 참여한 모든 채팅방을 찾습니다.
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.user.id = :userId AND p.isActive = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);
}
