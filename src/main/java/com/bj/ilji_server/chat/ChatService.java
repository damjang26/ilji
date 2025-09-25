package com.bj.ilji_server.chat;

import com.bj.ilji_server.chat.event.LeaveChatEvent;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ChatMessageRepository chatMessageRepository; // ChatMessage 저장용
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ChatRoom> findMyChatRooms(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail)); // Or handle as you see fit
        return chatRoomRepository.findChatRoomsByUserId(user.getId());
    }

    @Transactional
    public ChatRoom createChatRoom(List<Long> participantIds, String roomName, Long creatorId) {
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
                .creatorId(creatorId) // 생성자 ID 설정
                .build();

        // ChatParticipant 객체를 생성하여 채팅방과 사용자를 연결합니다.
        List<ChatParticipant> chatParticipants = participants.stream()
                .map(user -> ChatParticipant.builder()
                        .chatRoom(chatRoom)
                        .user(user)
                        .isActive(true) // 기본적으로 활성 상태
                        .build())
                .collect(Collectors.toList());

        // 생성된 ChatRoom과 ChatParticipant 정보를 저장합니다.
        chatRoomRepository.save(chatRoom);
        chatParticipantRepository.saveAll(chatParticipants);

        return chatRoom;
    }

    @Transactional
    public void leaveChatRoom(String roomId, Long leavingUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found with ID: " + roomId));

        User leavingUser = userRepository.findById(leavingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Leaving user not found with ID: " + leavingUserId));

        // 해당 채팅방에서 나가는 사용자의 ChatParticipant를 찾습니다.
        ChatParticipant participantToUpdate = chatParticipantRepository.findByChatRoomAndUser(chatRoom, leavingUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant of this chat room."));

        // isActive 상태를 false로 변경합니다.
        participantToUpdate.setActive(false);
        chatParticipantRepository.save(participantToUpdate);

        // 시스템 메시지 생성 및 저장
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRoomId(roomId);
        systemMessage.setSender("SYSTEM");
        systemMessage.setMessage(leavingUser.getName() + "님이 채팅방을 나갔습니다.");
        systemMessage.setTimestamp(LocalDateTime.now());
        systemMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
        chatMessageRepository.save(systemMessage);

        // 이벤트를 발행하여 Socket.IO 메시지 발송을 요청합니다.
        eventPublisher.publishEvent(new LeaveChatEvent(roomId, systemMessage));
    }
}