package com.bj.ilji_server.tag.controller;

import com.bj.ilji_server.tag.dto.TagCreateRequest;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.dto.TagUpdateRequest;
import com.bj.ilji_server.tag.service.TagService;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev/tags")
@RequiredArgsConstructor
public class DevTagController {

    private final TagService tagService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(User user) {
        if (user == null) {
            return userRepository.findById(4L)
                    .orElseThrow(() -> new RuntimeException("테스트 유저(ID=4)를 찾을 수 없습니다. DB를 확인해주세요."));
        }
        return user;
    }

    // [추가] 인증 문제를 우회하기 위해 사용자 ID를 직접 경로에서 받아 태그를 조회하는 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TagResponse>> getUserTagsById(@PathVariable Long userId) {
        return ResponseEntity.ok(tagService.getUserTagsById(userId));
    }

    // [수정] 인증을 우회하고 Request Body에 포함된 userId로 태그를 생성하는 API
    @PostMapping
    public ResponseEntity<TagResponse> createTagForDev(@RequestBody TagCreateRequest request) {
        // 클라이언트에서 보낸 userId가 request DTO에 포함되어 있어야 합니다.
        TagResponse response = tagService.createTagForDev(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@AuthenticationPrincipal User user, @PathVariable Long tagId) {
        User currentUser = getAuthenticatedUser(user);
        tagService.deleteTag(currentUser, tagId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @AuthenticationPrincipal User user,
            @PathVariable Long tagId,
            @RequestBody TagUpdateRequest request) {

        User currentUser = getAuthenticatedUser(user);
        TagResponse response = tagService.updateTag(currentUser, tagId, request);
        return ResponseEntity.ok(response);
    }

}
