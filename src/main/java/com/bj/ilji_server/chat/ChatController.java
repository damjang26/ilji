package com.bj.ilji_server.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/chat")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/list")
    public ResponseEntity<List<ChatRoom>> list(Authentication authentication) {
        // SecurityConfig에서 permitAll()로 설정했더라도, 토큰이 있으면 Authentication 객체가 주입됩니다.
        if (authentication == null) {
            // 토큰 없이 접근한 경우, 비어있는 목록이나 에러를 반환할 수 있습니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName(); // JWT에 저장된 사용자 ID를 가져옵니다.
        System.out.println(userId);
        List<ChatRoom> myChatRooms = chatService.findMyChatRooms(userId);
        return ResponseEntity.ok(myChatRooms);
    }
}
