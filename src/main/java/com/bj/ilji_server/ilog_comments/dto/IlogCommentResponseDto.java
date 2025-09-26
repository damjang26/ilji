package com.bj.ilji_server.ilog_comments.dto;

import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.user_profile.dto.UserProfileDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter // 자식 댓글 목록을 나중에 설정하기 위해 Setter를 추가합니다.
public class IlogCommentResponseDto {

    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean isDeleted;
    private boolean isLiked;
    private UserProfileDto writer;
    private List<IlogCommentResponseDto> children = new ArrayList<>(); // 대댓글 목록

    /**
     * JPQL에서 DTO로 직접 조회할 때 사용하는 생성자
     * @param comment 댓글 엔티티 (JOIN FETCH로 UserProfile 포함)
     * @param isLiked 현재 사용자의 '좋아요' 여부
     */
    public IlogCommentResponseDto(IlogComment comment, boolean isLiked) {
        this.commentId = comment.getId();
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.likeCount = comment.getLikeCount();
        this.isDeleted = comment.isDeleted();
        this.isLiked = isLiked;
        this.writer = UserProfileDto.fromEntity(comment.getUserProfile());

        // 자식 댓글이 있는 경우, 자식 댓글도 DTO로 변환합니다.
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            this.children = comment.getChildren().stream()
                    .map(child -> {
                        // ✅ [수정] 대댓글의 '좋아요' 여부 계산 로직 수정
                        // JPQL 생성자에서는 '현재 로그인한 사용자'의 ID를 알 수 없으므로,
                        // 여기서 정확한 'isLiked' 상태를 계산하는 것은 어렵고 N+1 문제를 유발할 수 있습니다.
                        // 따라서 우선 'false'로 초기화하고, 필요 시 서비스 레이어에서 후처리하는 것을 권장합니다.
                        // 이전 코드는 부모 댓글 작성자 기준으로 계산하여 로직에 오류가 있었습니다.
                        return new IlogCommentResponseDto(child, false);
                    })
                    .collect(Collectors.toList());
        }
    }

    public static IlogCommentResponseDto from(IlogComment comment, boolean isLiked) {
        return new IlogCommentResponseDto(comment, isLiked);
    }
}