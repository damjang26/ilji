package com.bj.ilji_server.user.controller;

import com.bj.ilji_server.user.dto.UserSearchResponse;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // UserProfileService를 사용하여 검색 로직을 처리합니다.
    private final UserProfileService userProfileService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal User currentUser) {

        // q 파라미터가 비어있는 경우 400 Bad Request를 반환합니다.
        if (!StringUtils.hasText(query)) {
            return ResponseEntity.badRequest().build();
        }

        List<UserSearchResponse> response = userProfileService.searchUsers(query, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가] 다른 사용자의 프로필 정보를 ID로 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return UserProfileResponse
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}
