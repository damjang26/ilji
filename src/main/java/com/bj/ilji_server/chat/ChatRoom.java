package com.bj.ilji_server.chat;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    private String roomId;

    private String roomName; // 채팅방 이름

    @Enumerated(EnumType.STRING)
    private RoomType roomType; // 채팅방 타입

    private Long creatorId; // 채팅방 생성자 ID (이 필드가 누락되어 있었습니다.)

    // ChatRoom 하나는 여러 명의 참여자(ChatParticipant)를 가짐
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatParticipant> participants = new ArrayList<>();

    private LocalDateTime createdAt;

    public enum RoomType {
        ONE_ON_ONE,
        GROUP
    }
}
