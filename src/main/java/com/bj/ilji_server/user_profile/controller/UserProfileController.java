package com.bj.ilji_server.user_profile.controller;

import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import com.bj.ilji_server.user_profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles") // RESTful 스타일에 맞춰 복수형으로 변경 (schedule -> schedules 처럼)
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<Void> updateUserProfile(@PathVariable Long userId,
                                                  @RequestBody UserProfileUpdateRequest request) {
        System.out.println("✅ [Controller] DTO 수신 >> accountPrivate: " + request.isPrivate());

        userProfileService.updateUserProfile(userId, request);
        return ResponseEntity.ok().build();
    }
}