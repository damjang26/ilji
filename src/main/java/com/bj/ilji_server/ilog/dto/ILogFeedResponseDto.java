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
public class ILogFeedResponseDto {

    private final Long id;
    private final Long writerId;
    private final String writerNickname;
    private final String writerProfileImage;
    private final String content;
    private final List<String> images;
    private final String friendTags;
    private final String tags;
    private final int likeCount;
    private final int commentCount;


    private final ILog.Visibility visibility;
    private final LocalDate logDate;
    private final LocalDateTime createdAt;


    private final boolean isLiked;
    private final BestCommentDto bestComment;

    // ✅ [신규] 베스트 댓글 정보를 담는 내부 DTO
    @Getter
    public static class BestCommentDto {
        private final Long commentId;
        private final String content;
        private final String writerNickname;

        public static BestCommentDto fromEntity(IlogComment comment) {
            if (comment == null) {
                return null;
            }
            return new BestCommentDto(comment.getId(), comment.getContent(), comment.getUserProfile().getNickname());
        }

        // Builder를 사용하지 않으므로, 모든 필드를 받는 생성자를 명시적으로 추가합니다.
        @Builder
        public BestCommentDto(Long commentId, String content, String writerNickname) {
            this.commentId = commentId;
            this.content = content;
            this.writerNickname = writerNickname;
        }
    }

    /**
     * JPQL에서 DTO로 직접 조회할 때 사용하는 생성자 (N+1 문제 해결용)
     * Lombok의 @Builder를 이 생성자에 적용하여, Builder 패턴과 JPQL 직접 조회를 동시에 지원합니다.
     * @param iLog 조회된 ILog 엔티티 (JOIN FETCH 되어 UserProfile 포함)
     * @param bestComment JPQL 서브쿼리로 조회된 베스트 댓글
     * @param isLiked JPQL 서브쿼리로 계산된 '좋아요' 여부
     */
    @Builder
    public ILogFeedResponseDto(ILog iLog, IlogComment bestComment, boolean isLiked) {
        // UserProfile이 null일 경우를 대비한 방어 코드
        UserProfile userProfile = iLog.getUserProfile();
        this.writerId = (userProfile != null) ? userProfile.getUserId() : null;
        this.writerNickname = (userProfile != null) ? userProfile.getNickname() : "알 수 없는 사용자";
        this.writerProfileImage = (userProfile != null) ? userProfile.getProfileImage() : null;

        this.id = iLog.getId();
        this.content = iLog.getContent();
        this.friendTags = iLog.getFriendTags();
        this.tags = iLog.getTags();
        this.likeCount = iLog.getLikeCount();
        this.commentCount = iLog.getCommentCount();
        this.visibility = iLog.getVisibility();
        this.logDate = iLog.getLogDate();
        this.createdAt = iLog.getCreatedAt();
        this.isLiked = isLiked;
        this.bestComment = BestCommentDto.fromEntity(bestComment);

        // 이미지 URL 파싱
        List<String> imageUrls = Collections.emptyList();
        if (iLog.getImgUrl() != null && !iLog.getImgUrl().isBlank()) {
            try {
                imageUrls = new ObjectMapper().readValue(iLog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException ignored) {} // 생성자에서는 예외를 던지기 어려우므로 무시하거나 로깅
        }
        this.images = imageUrls;
    }

    /**
     * ✅ [신규] 서비스 레이어에서 엔티티를 DTO로 변환할 때 사용하는 정적 팩토리 메서드입니다.
     * '좋아요' 여부를 동적으로 계산합니다.
     * @param iLog 조회된 ILog 엔티티
     * @param currentUserId 현재 로그인한 사용자의 ID (비로그인 시 null)
     * @return 생성된 ILogFeedResponseDto 객체
     */
    public static ILogFeedResponseDto fromEntity(ILog iLog, Long currentUserId) {
        boolean isLiked = false;
        if (currentUserId != null && iLog.getLikes() != null) {
            isLiked = iLog.getLikes().stream()
                    .anyMatch(like -> like.getUserProfile().getUser().getId().equals(currentUserId));
        }
        // 베스트 댓글은 이 컨텍스트에서 조회하지 않으므로 null을 전달합니다.
        return new ILogFeedResponseDto(iLog, null, isLiked);
    }

}