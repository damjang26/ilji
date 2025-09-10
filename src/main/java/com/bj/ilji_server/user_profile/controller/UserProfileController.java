package com.bj.ilji_server.user_profile.controller;

import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import com.bj.ilji_server.user_profile.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse response = userProfileService.getUserProfile(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<String> updateUserProfile(@AuthenticationPrincipal User user,
                                                    @RequestPart("request") String requestStr,
                                                    @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                                    @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
                                                    @RequestParam(value = "revertProfileImage", defaultValue = "false") boolean revertProfileImage,
                                                    @RequestParam(value = "revertBannerImage", defaultValue = "false") boolean revertBannerImage) {
        try {
            UserProfileUpdateRequest request = objectMapper.readValue(requestStr, UserProfileUpdateRequest.class);

            userProfileService.updateUserProfile(user, request, profileImage, bannerImage, revertProfileImage, revertBannerImage);

            return ResponseEntity.ok("프로필이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            // 어떤 오류가 발생하는지 정확히 파악하기 위해 전체 스택 트레이스를 로그로 남깁니다.
            log.error("프로필 업데이트 중 심각한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 업데이트 중 서버 오류가 발생했습니다.");
        }
    }
}