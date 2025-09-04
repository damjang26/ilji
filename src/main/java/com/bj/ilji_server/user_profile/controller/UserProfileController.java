package com.bj.ilji_server.user_profile.controller;

import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import com.bj.ilji_server.user_profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/user/{userId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> updateUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser,
            @RequestPart("request") UserProfileUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage) throws IOException {

        // --- 디버깅 로그 1단계 ---
        System.out.println("[Controller] 1. updateUserProfile 메서드 진입");
        System.out.println("[Controller]    - 받은 DTO: " + request);
        System.out.println("[Controller]    - 프로필 이미지 수신 여부: " + (profileImage != null && !profileImage.isEmpty()));
        System.out.println("[Controller]    - 배너 이미지 수신 여부: " + (bannerImage != null && !bannerImage.isEmpty()));




        // URL의 userId와 현재 로그인한 사용자의 ID가 일치하는지 확인 (보안 강화)
        if (!userId.equals(currentUser.getId())) {
            // 403 Forbidden: 권한 없음
            return ResponseEntity.status(403).build();
        }

        // 서비스 레이어에 이미지 파일과 DTO를 모두 전달합니다.
        userProfileService.updateUserProfile(userId, request, profileImage, bannerImage);
        return ResponseEntity.ok().build();
    }
}