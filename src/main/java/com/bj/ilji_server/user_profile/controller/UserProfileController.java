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

    @PutMapping(
            value = "/user/{userId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Void> updateUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser,
            @RequestPart("request") UserProfileUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            // ✅ 기본 이미지 복원 시 URL 값도 받을 수 있도록 추가
            @RequestPart(value = "profileImageUrl", required = false) String profileImageUrl,
            @RequestPart(value = "bannerImageUrl", required = false) String bannerImageUrl
    ) throws IOException {

        // --- 디버깅 로그 ---
        System.out.println("[Controller] updateUserProfile 호출");
        System.out.println("  - profileImage 수신 여부: " + (profileImage != null && !profileImage.isEmpty()));
        System.out.println("  - bannerImage 수신 여부: " + (bannerImage != null && !bannerImage.isEmpty()));
        System.out.println("  - profileImageUrl 값: " + profileImageUrl);
        System.out.println("  - bannerImageUrl 값: " + bannerImageUrl);

        // 보안 체크
        if (!userId.equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 서비스 호출
        userProfileService.updateUserProfile(
                userId,
                request,
                profileImage,
                bannerImage,
                profileImageUrl,
                bannerImageUrl
        );

        return ResponseEntity.ok().build();
    }
}
