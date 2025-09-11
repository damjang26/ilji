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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(User user) {
        if (user == null) {
            return userRepository.findById(4L)
                    .orElseThrow(() -> new RuntimeException("테스트 유저(ID=4)를 찾을 수 없습니다. DB를 확인해주세요."));
        }
        return user;
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getTags(
            @RequestParam(name = "userId", required = false) Long ownerId,
            @AuthenticationPrincipal User viewer) {

        User currentViewer = getAuthenticatedUser(viewer);
        Long targetUserId = (ownerId != null) ? ownerId : currentViewer.getId();
        List<TagResponse> tags = tagService.getVisibleTags(targetUserId, currentViewer);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@AuthenticationPrincipal User user, @RequestBody TagCreateRequest request) {
        User currentUser = getAuthenticatedUser(user);
        TagResponse response = tagService.createTag(currentUser, request);
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
