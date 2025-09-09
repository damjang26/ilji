package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.user_profile.dto.UserFeedDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class ILogFeedResponseDto {

    private Long id;
    private UserFeedDto user; // 사용자 정보를 담을 객체
    private String content;
    private List<String> images;
    private ILog.Visibility visibility;
    private LocalDateTime createdAt;

    @Builder
    public ILogFeedResponseDto(Long id, UserFeedDto user, String content, List<String> images, ILog.Visibility visibility, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.images = images;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    public static ILogFeedResponseDto fromEntity(ILog iLog, ObjectMapper objectMapper) {
        List<String> imageUrls = Collections.emptyList();
        if (iLog.getImgUrl() != null && !iLog.getImgUrl().isBlank()) {
            try {
                imageUrls = objectMapper.readValue(iLog.getImgUrl(), new TypeReference<>() {});
            } catch (IOException e) {
                // 로그를 남기거나 예외 처리를 할 수 있습니다. 지금은 빈 리스트로 대체합니다.
            }
        }

        return ILogFeedResponseDto.builder()
                .id(iLog.getId())
                .user(UserFeedDto.fromEntity(iLog.getUserProfile()))
                .content(iLog.getContent())
                .images(imageUrls) // JSON 문자열을 List로 변환
                .visibility(iLog.getVisibility())
                .createdAt(iLog.getCreatedAt())
                .build();
    }
}