package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder // 생성자가 아닌 클래스 레벨로 이동하여 final 필드와 함께 사용합니다.
public class ILogFeedResponseDto {

    private final Long id;
    // ✅ [수정] UserFeedDto 대신 작성자 정보를 최상위 필드로 평탄화합니다.
    private final Long writerId;
    private final String writerNickname;
    private final String writerProfileImage;
    private final String content;
    private final List<String> images;
    // ✅ [추가] 지적해주신 대로, 피드에서도 태그, 좋아요, 댓글 수를 보여주기 위해 필드를 추가합니다.
    private final String friendTags;
    private final String tags;
    private final int likeCount;
    private final int commentCount;



    private final ILog.Visibility visibility;
    private final LocalDate logDate; // ✅ [추가] 일기 기록 날짜 필드
    private final LocalDateTime createdAt;


    private final boolean isLiked;
    private final BestCommentDto bestComment; // ✅ [신규] 베스트 댓글 정보

    // ✅ [신규] 베스트 댓글 정보를 담는 내부 DTO
    @Getter
    @Builder
    public static class BestCommentDto {
        private final Long commentId;
        private final String content;
        private final String writerNickname;

        public static BestCommentDto fromEntity(IlogComment comment) {
            if (comment == null) {
                return null;
            }
            return BestCommentDto.builder()
                    .commentId(comment.getId())
                    .content(comment.getContent())
                    .writerNickname(comment.getUserProfile().getNickname())
                    .build();
        }
    }

    public static ILogFeedResponseDto fromEntity(ILog iLog, IlogComment bestComment, ObjectMapper objectMapper, Long currentUserId) {
        List<String> imageUrls = Collections.emptyList();
        if (iLog.getImgUrl() != null && !iLog.getImgUrl().isBlank()) {
            try {
                imageUrls = objectMapper.readValue(iLog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException e) {
                // ✅ [개선] 이미지 URL 파싱 실패 시 로그를 남겨 문제를 추적할 수 있도록 합니다.
                System.err.println("Failed to parse image URLs for feed: " + e.getMessage());
            }
        }

        // ✅ [개선] UserProfile이 null일 경우를 대비한 방어 코드
        UserProfile userProfile = iLog.getUserProfile();
        Long writerId = (userProfile != null) ? userProfile.getUserId() : null;
        String writerNickname = (userProfile != null) ? userProfile.getNickname() : "알 수 없는 사용자";
        String writerProfileImage = (userProfile != null) ? userProfile.getProfileImage() : null;

        boolean isLiked = currentUserId != null && iLog.getLikes().stream()
                .anyMatch(like -> like.getUserProfile().getUserId().equals(currentUserId));


        return ILogFeedResponseDto.builder()
                .id(iLog.getId())
                .writerId(writerId)
                .writerNickname(writerNickname)
                .writerProfileImage(writerProfileImage)
                .content(iLog.getContent())
                .images(imageUrls) // JSON 문자열을 List로 변환
                // ✅ [추가] ILog 엔티티에서 빠진 필드 정보를 채워줍니다.
                .friendTags(iLog.getFriendTags())
                .tags(iLog.getTags())
                .likeCount(iLog.getLikeCount())
                .commentCount(iLog.getCommentCount())
                .visibility(iLog.getVisibility())
                .logDate(iLog.getLogDate())
                .createdAt(iLog.getCreatedAt())
                .isLiked(isLiked)
                .bestComment(BestCommentDto.fromEntity(bestComment)) // ✅ [신규] 베스트 댓글 DTO 생성
                .build();
    }
}