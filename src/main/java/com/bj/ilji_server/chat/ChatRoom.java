package com.bj.ilji_server.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1Id", "user2Id"})
})
public class ChatRoom {

    @Id
    private String roomId;

    private String user1Id;
    private String user2Id;

    private LocalDateTime createdAt = LocalDateTime.now();
}
