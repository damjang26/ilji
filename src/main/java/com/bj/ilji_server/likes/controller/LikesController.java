package com.bj.ilji_server.likes.controller;

import com.bj.ilji_server.likes.dto.LikerInfoDTO;
import com.bj.ilji_server.likes.service.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ilogs/{ilogId}/like")
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    @PostMapping
    public ResponseEntity<Boolean> toggleLike(@PathVariable Long ilogId, @AuthenticationPrincipal String userId) {
        boolean isLiked = likesService.toggleLike(ilogId, Long.parseLong(userId));
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping
    public ResponseEntity<List<LikerInfoDTO>> getLikers(@PathVariable Long ilogId) {
        List<LikerInfoDTO> likers = likesService.getLikers(ilogId);
        return ResponseEntity.ok(likers);
    }
}
