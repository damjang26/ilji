package com.bj.ilji_server.tag.controller;

import com.bj.ilji_server.tag.dto.TagCreateRequest;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.service.TagService;
import com.bj.ilji_server.user.entity.User;
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

    @GetMapping
    public ResponseEntity<List<TagResponse>> getTags(
            @RequestParam(name = "userId", required = false) Long ownerId,
            @AuthenticationPrincipal User viewer) {

        Long targetUserId = (ownerId != null) ? ownerId : viewer.getId();
        List<TagResponse> tags = tagService.getVisibleTags(targetUserId, viewer);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@AuthenticationPrincipal User user, @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.createTag(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@AuthenticationPrincipal User user, @PathVariable Long tagId) {
        tagService.deleteTag(user, tagId);
        return ResponseEntity.noContent().build();
    }
}
