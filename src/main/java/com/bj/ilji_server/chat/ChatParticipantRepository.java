package com.bj.ilji_server.chat;

import com.bj.ilji_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);
}
