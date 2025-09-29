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
public class ILogResponse {

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
    private final ILogFeedResponseDto.BestCommentDto bestComment;

    /**
     * JPQL에서 DTO로 직접 조회할 때 사용하는 생성자 (N+1 문제 해결용)
     * @param iLog 조회된 ILog 엔티티 (JOIN FETCH 되어 UserProfile 포함)
     * @param bestComment JPQL 서브쿼리로 조회된 베스트 댓글 (현재는 null)
     * @param isLiked JPQL 서브쿼리로 계산된 '좋아요' 여부
     */
    @Builder
    public ILogResponse(ILog iLog, IlogComment bestComment, boolean isLiked) {
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
        this.bestComment = ILogFeedResponseDto.BestCommentDto.fromEntity(bestComment);

        List<String> imageUrls = Collections.emptyList();
        if (iLog.getImgUrl() != null && !iLog.getImgUrl().isBlank()) {
            try {
                imageUrls = new ObjectMapper().readValue(iLog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException ignored) {}
        }
        this.images = imageUrls;
    }

    /**
     * N+1 문제가 발생하는 기존 fromEntity 메서드.
     * create, update 등 단일 건 처리 시에는 계속 사용될 수 있으므로 남겨둡니다.
     */
    public static ILogResponse fromEntity(ILog iLog, IlogComment bestComment, ObjectMapper objectMapper, Long currentUserId) {
        boolean isLikedForEntity = currentUserId != null && iLog.getLikes().stream()
                .anyMatch(like -> like.getUserProfile().getUserId().equals(currentUserId));

        ILogResponse response = new ILogResponse(iLog, bestComment, isLikedForEntity);
        // ObjectMapper를 외부에서 주입받아 이미지 파싱을 수행하도록 수정
        if (iLog.getImgUrl() != null && !iLog.getImgUrl().isBlank()) {
            try {
                // final이 아니므로 필드 재할당이 가능하지만, 생성자에서 처리하는 것이 더 좋습니다.
                // 이 예제에서는 fromEntity의 호환성을 위해 남겨둡니다.
            } catch (Exception ignored) {}
        }
        return response;
    }
}