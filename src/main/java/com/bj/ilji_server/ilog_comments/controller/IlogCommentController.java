package com.bj.ilji_server.ilog_comments.controller;

import com.bj.ilji_server.ilog_comments.dto.IlogCommentCreateRequest;
import com.bj.ilji_server.ilog_comments.dto.IlogCommentDto;
import com.bj.ilji_server.ilog_comments.service.IlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bj.ilji_server.user.entity.User;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IlogCommentController {

    private final IlogCommentService ilogCommentService;

    /**
     * 특정 일기의 모든 댓글을 계층 구조로 조회하는 API
     * @param ilogId 일기 ID
     * @return 댓글 목록
     */
    @GetMapping("/ilogs/{ilogId}/comments")
    public ResponseEntity<List<IlogCommentDto>> getComments(
            @PathVariable Long ilogId,
            // 'sortBy' 파라미터를 받습니다. 값이 없으면 기본으로 "likes"를 사용합니다.
            @RequestParam(name = "sortBy", defaultValue = "likes") String sortBy,
            // ✅ [수정] 현재 로그인한 사용자 정보를 받아옵니다. 비로그인 사용자의 경우 null이 됩니다.
            @AuthenticationPrincipal User user
    ) {
        // ✅ [수정] 서비스 메서드에 user 객체를 전달합니다.
        List<IlogCommentDto> comments = ilogCommentService.getCommentsByIlogId(ilogId, sortBy, user);
        return ResponseEntity.ok(comments);
    }

    /**
     * 새로운 댓글 또는 대댓글을 작성하는 API
     * @param ilogId 일기 ID
     * @param request 댓글 내용 및 부모 댓글 ID
     * @param user 현재 로그인한 사용자 정보 (Spring Security)
     * @return 생성된 댓글 정보
     */
    @PostMapping("/ilogs/{ilogId}/comments")
    public ResponseEntity<IlogCommentDto> createComment(
            @PathVariable Long ilogId,
            @RequestBody IlogCommentCreateRequest request,
            @AuthenticationPrincipal User user // Spring Security를 통해 주입
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인하지 않은 사용자 접근 차단
        }
        IlogCommentDto createdComment = ilogCommentService.createComment(ilogId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * 댓글을 삭제하는 API
     * @param commentId 삭제할 댓글 ID
     * @param user 현재 로그인한 사용자 정보 (Spring Security)
     * @return 응답 없음
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user // Spring Security를 통해 주입
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인하지 않은 사용자 접근 차단
        }
        ilogCommentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }
}