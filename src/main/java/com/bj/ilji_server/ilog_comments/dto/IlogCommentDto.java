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
    private List<IlogCommentDto> replies; // 대댓글 목록

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static IlogCommentDto from(IlogComment comment) {
        return IlogCommentDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .writer(WriterDto.from(comment.getUserProfile()))
                // 자식 댓글(대댓글)들도 재귀적으로 DTO로 변환
                // 논리적으로 삭제되지 않은 자식 댓글만 DTO로 변환하여 목록에 포함
                .replies(comment.getChildren().stream()
                        .filter(child -> !child.isDeleted())
                        .map(IlogCommentDto::from)
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