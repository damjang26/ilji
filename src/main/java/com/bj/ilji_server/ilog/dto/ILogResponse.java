package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
// @Setter를 제거하여 DTO를 불변(Immutable) 객체로 만듭니다.
// 생성자를 통해서만 값이 할당되므로 데이터의 일관성을 보장할 수 있습니다.
@Builder
public class ILogResponse {

    private final Long id;
    // ✅ [수정] 요청하신 대로 작성자 정보를 구체적인 필드로 추가합니다.
    private final Long writerId;
    private final String writerNickname;
    private final String writerProfileImage;

    private final LocalDate logDate;
    private final String content;
    private final List<String> images; // JSON 문자열 대신 이미지 URL 리스트
    private final ILog.Visibility visibility;
    private final String friendTags;
    private final String tags;
    private final int likeCount;
    private final int commentCount;
    private final LocalDateTime createdAt;

    // Entity → DTO 변환 편의 메서드
    public static ILogResponse fromEntity(ILog ilog, ObjectMapper objectMapper) {
        List<String> imageUrls = Collections.emptyList();
        if (ilog.getImgUrl() != null && !ilog.getImgUrl().isBlank()) {
            try {
                imageUrls = objectMapper.readValue(ilog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException e) {
                // 이미지 URL 파싱 실패 시 로그를 남겨 문제를 추적할 수 있도록 합니다.
                System.err.println("Failed to parse image URLs: " + e.getMessage());
            }
        }

        // UserProfile이 null일 경우를 대비한 방어 코드
        UserProfile userProfile = ilog.getUserProfile();
        Long writerId = (userProfile != null) ? userProfile.getUserId() : null;
        String writerNickname = (userProfile != null) ? userProfile.getNickname() : "알 수 없는 사용자";
        String writerProfileImage = (userProfile != null) ? userProfile.getProfileImage() : null;

        return ILogResponse.builder()
                .id(ilog.getId())
                // ✅ [수정] UserProfile 엔티티에서 추출한 작성자 정보를 DTO에 담습니다.
                .writerId(writerId)
                .writerNickname(writerNickname)
                .writerProfileImage(writerProfileImage)

                .logDate(ilog.getLogDate())
                .content(ilog.getContent())
                .images(imageUrls)
                .visibility(ilog.getVisibility())
                .friendTags(ilog.getFriendTags())
                .tags(ilog.getTags())
                .likeCount(ilog.getLikeCount())
                .commentCount(ilog.getCommentCount())
                .createdAt(ilog.getCreatedAt())
                .build();
    }
}
