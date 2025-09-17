package com.bj.ilji_server.chat;

import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/chat")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private final ChatMessageService chatMessageService;

    @GetMapping("/list")
    public ResponseEntity<List<ChatRoom>> list(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        List<ChatRoom> myChatRooms = chatService.findMyChatRooms(userId);
        return ResponseEntity.ok(myChatRooms);
    }

    @GetMapping("/messages/{roomId}")
    public List<ChatMessage> getChatMessages(@PathVariable String roomId) {
        return chatMessageService.getMessagesByRoomId(roomId);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody CreateChatRoomRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        // 요청에 현재 사용자 ID가 포함되어 있지 않으면 추가합니다.
        if (!request.getUserIds().contains(currentUser.getId())) {
            request.getUserIds().add(currentUser.getId());
        }

        ChatRoom chatRoom = chatService.createChatRoom(request.getUserIds(), request.getRoomName());
        return ResponseEntity.ok(chatRoom);
    }
}