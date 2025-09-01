package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ILogResponse {

    private Long id;
    private Long userId; // 작성자 정보 대신 ID만
    private LocalDate iLogDate;
    private String content;
    private String imgUrl;
    private ILog.Visibility visibility;
    private String friendTags;
    private String tags;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    // Entity → DTO 변환 편의 메서드
    public static ILogResponse fromEntity(ILog ilog) {
        return ILogResponse.builder()
                .id(ilog.getId())
                .userId(ilog.getUser().getId()) // User 객체 대신 ID만
                .iLogDate(ilog.getILogDate())
                .content(ilog.getContent())
                .imgUrl(ilog.getImgUrl())
                .visibility(ilog.getVisibility())
                .friendTags(ilog.getFriendTags())
                .tags(ilog.getTags())
                .likeCount(ilog.getLikeCount())
                .commentCount(ilog.getCommentCount())
                .createdAt(ilog.getCreatedAt())
                .build();
    }
}
