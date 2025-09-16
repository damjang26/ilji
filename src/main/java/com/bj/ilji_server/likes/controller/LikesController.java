package com.bj.ilji_server.likes.controller;

import com.bj.ilji_server.ilog.dto.ILogFeedResponseDto;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.service.ILogService;
import com.bj.ilji_server.likes.dto.LikerInfoDTO;
import com.bj.ilji_server.likes.service.LikesService;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ilogs/{ilogId}/like")
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    private final ILogService ilogService;

    @PostMapping
    public ResponseEntity<Boolean> toggleLike(@PathVariable Long ilogId, @AuthenticationPrincipal User user) {
        System.out.println(" >>>>>>>>>>>>>>>>>>> " + user.getId());
        boolean isLiked = likesService.toggleLike(ilogId, user.getId());
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping
    public ResponseEntity<List<LikerInfoDTO>> getLikers(@PathVariable Long ilogId) {
        List<LikerInfoDTO> likers = likesService.getLikers(ilogId);
        return ResponseEntity.ok(likers);
    }


}
