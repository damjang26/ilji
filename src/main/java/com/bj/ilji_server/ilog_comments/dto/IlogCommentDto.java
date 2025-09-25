package com.bj.ilji_server.ilog_comments.dto;

import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class IlogCommentDto {
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private WriterDto writer;
    private int likeCount; // ✅ [신규] 좋아요 수
    private boolean isLiked; // ✅ [추가] 현재 사용자의 '좋아요' 여부
    private List<IlogCommentDto> replies; // 대댓글 목록

    /**
     * 엔티티를 DTO로 변환하는 정적 메서드 (재귀 호출을 통해 대댓글까지 처리)
     * @param comment 변환할 댓글 엔티티
     * @param isLiked 현재 사용자의 '좋아요' 여부
     * @return 변환된 DTO
     */
    public static IlogCommentDto from(IlogComment comment, boolean isLiked) {
        return IlogCommentDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount()) // ✅ [신규] 좋아요 수 매핑
                .isLiked(isLiked) // ✅ [추가] isLiked 필드 설정
                .writer(WriterDto.from(comment.getUserProfile()))
                // 자식 댓글(대댓글)들도 재귀적으로 DTO로 변환
                // 논리적으로 삭제되지 않은 자식 댓글만 DTO로 변환하여 목록에 포함
                .replies(comment.getChildren().stream()
                        .filter(child -> !child.isDeleted())
                        // TODO: 자식 댓글의 isLiked 상태도 확인해야 합니다.
                        // 우선 false로 설정하고, 필요 시 자식 댓글에 대한 isLiked 정보도 조회하는 로직 추가가 필요합니다.
                        .map(child -> IlogCommentDto.from(child, false))
                        .collect(Collectors.toList()))
                .build();
    }

    // 댓글 작성자 정보를 담는 내부 DTO
    @Getter
    @Builder
    private static class WriterDto {
        private Long userId;
        private String nickname;
        private String profileImage;

        public static WriterDto from(com.bj.ilji_server.user_profile.entity.UserProfile userProfile) {
            return WriterDto.builder()
                    .userId(userProfile.getUserId())
                    .nickname(userProfile.getNickname())
                    .profileImage(userProfile.getProfileImage())
                    .build();
        }
    }
}