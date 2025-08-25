package com.bj.ilji_server.user.controller;

import com.bj.ilji_server.user.dto.AuthResponse;
import com.bj.ilji_server.user.dto.GoogleLoginRequest;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            AuthResponse authResponse = authService.loginWithGoogle(request.getToken());
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID Token: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal 어노테이션이 JWT 필터에서 인증된 사용자 정보를 가져와 줍니다.
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        // 프론트엔드가 기대하는 { user: { ... } } 형태의 응답을 생성합니다.
        return ResponseEntity.ok(Map.of("user", new AuthResponse.UserDto(user)));
    }
}