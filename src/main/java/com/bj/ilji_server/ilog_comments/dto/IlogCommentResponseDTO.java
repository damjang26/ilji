package com.bj.ilji_server.ilog_comments.dto;

import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IlogCommentResponseDTO {
    private Long commentId;
    private String content;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private LocalDateTime createdAt;
    private Long likeCount;
    private boolean isLiked; // ✅ [추가] 현재 사용자의 '좋아요' 여부

    public static IlogCommentResponseDTO toDTO(IlogComment comment, Long likeCount, boolean isLiked) {
        return IlogCommentResponseDTO.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writerId(comment.getUserProfile().getUserId())
                .writerNickname(comment.getUserProfile().getNickname())
                .writerProfileImage(comment.getUserProfile().getUser().getPicture())
                .createdAt(comment.getCreatedAt())
                .likeCount(likeCount)
                .isLiked(isLiked) // ✅ [추가] DTO 변환 시 isLiked 값 설정
                .build();
    }
}