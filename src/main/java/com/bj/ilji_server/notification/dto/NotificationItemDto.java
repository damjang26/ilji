package com.bj.ilji_server.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationItemDto {
    private Long id;
    private String type;         // ENUM 문자열 (예: COMMENT_CREATED)
    private String entityType;   // ENUM 문자열 (예: POST)
    private Long entityId;
    private String title;
    private String body;
    private String linkUrl;
    private String status;       // ENUM 문자열 (NEW | READ | ARCHIVED)
    private OffsetDateTime createdAt;
    private Map<String, Object> meta; // meta_json 파싱 결과
}
