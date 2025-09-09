package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ILogResponse {

    private Long id;
    private Long userId; // 작성자 정보 대신 ID만
    private LocalDate logDate;
    private String content;
    private List<String> images; // JSON 문자열 대신 이미지 URL 리스트
    private ILog.Visibility visibility;
    private String friendTags;
    private String tags;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    // Entity → DTO 변환 편의 메서드
    public static ILogResponse fromEntity(ILog ilog, ObjectMapper objectMapper) {
        List<String> imageUrls = Collections.emptyList();
        if (ilog.getImgUrl() != null && !ilog.getImgUrl().isBlank()) {
            try {
                imageUrls = objectMapper.readValue(ilog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException e) {
                // 예외 처리
            }
        }

        return ILogResponse.builder()
                .id(ilog.getId())
                .userId(ilog.getUserProfile().getUserId()) // UserProfile에서 UserId 가져오기
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
