package com.bj.ilji_server.chat;

import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional(readOnly = true)
    public List<ChatRoom> findMyChatRooms(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail)); // Or handle as you see fit
        return chatRoomRepository.findChatRoomsByUserId(user.getId());
    }

    @Transactional
    public ChatRoom createChatRoom(List<Long> participantIds, String roomName) {
        // 요청한 사용자 ID로부터 User 엔티티 목록을 조회합니다.
        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new IllegalArgumentException("Some users not found");
        }

        // 채팅방을 생성합니다.
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName)
                .roomType(participants.size() > 2 ? ChatRoom.RoomType.GROUP : ChatRoom.RoomType.ONE_ON_ONE)
                .createdAt(LocalDateTime.now())
                .build();

        // ChatParticipant 객체를 생성하여 채팅방과 사용자를 연결합니다.
        List<ChatParticipant> chatParticipants = participants.stream()
                .map(user -> ChatParticipant.builder()
                        .chatRoom(chatRoom)
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        // 생성된 ChatRoom과 ChatParticipant 정보를 저장합니다.
        chatRoomRepository.save(chatRoom);
        chatParticipantRepository.saveAll(chatParticipants);

        return chatRoom;
    }
}
