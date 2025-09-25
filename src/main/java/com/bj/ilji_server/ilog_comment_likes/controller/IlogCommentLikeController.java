package com.bj.ilji_server.ilog_comment_likes.controller;

import com.bj.ilji_server.ilog_comment_likes.service.IlogCommentLikeService;
import com.bj.ilji_server.likes.dto.LikerInfoDTO;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments/{commentId}/like")
@RequiredArgsConstructor
public class IlogCommentLikeController {

    private final IlogCommentLikeService ilogCommentLikeService;

    /**
     * 특정 댓글에 대한 '좋아요'를 토글(추가/삭제)합니다.
     * @param commentId 대상 댓글 ID
     * @param user 현재 인증된 사용자 정보
     * @return '좋아요'가 추가되면 true, 삭제되면 false
     */
    @PostMapping
    public ResponseEntity<Boolean> toggleCommentLike(@PathVariable Long commentId, @AuthenticationPrincipal User user) {
        boolean isLiked = ilogCommentLikeService.toggleCommentLike(commentId, user.getId());
        return ResponseEntity.ok(isLiked);
    }

    /**
     * 특정 댓글에 '좋아요'를 누른 사용자 목록을 조회합니다.
     * @param commentId 대상 댓글 ID
     * @return '좋아요'를 누른 사용자 정보 리스트
     */
    @GetMapping
    public ResponseEntity<List<LikerInfoDTO>> getCommentLikers(@PathVariable Long commentId) {
        List<LikerInfoDTO> likers = ilogCommentLikeService.getCommentLikers(commentId);
        return ResponseEntity.ok(likers);
    }
}