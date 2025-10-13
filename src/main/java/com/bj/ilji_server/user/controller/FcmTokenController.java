package com.bj.ilji_server.user.controller;

import com.bj.ilji_server.user.dto.FcmTokenDto;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@AuthenticationPrincipal User user, @RequestBody FcmTokenDto fcmTokenDto) {
        fcmTokenService.updateFcmToken(user.getId(), fcmTokenDto.getFcmToken());
        return ResponseEntity.ok().build();
    }
}
