package com.bj.ilji_server.notification.dto;

import com.bj.ilji_server.notification.entity.Notification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public final class NotificationDtoMapper {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<Map<String,Object>> MAP_TYPE = new TypeReference<>() {};

    private NotificationDtoMapper() {}

    public static NotificationItemDto toDto(Notification n) {
        return NotificationItemDto.builder()
                .id(n.getId())
                .type(n.getType() == null ? null : n.getType().name())
                .entityType(n.getEntityType() == null ? null : n.getEntityType().name())
                .entityId(n.getEntityId())
                .title(n.getMessageTitle())
                .body(n.getMessageBody())
                .linkUrl(n.getLinkUrl())
                .status(n.getStatus() == null ? null : n.getStatus().name())
                .createdAt(n.getCreatedAt())
                .meta(parseMeta(n.getMetaJson()))
                .build();
    }

    private static Map<String, Object> parseMeta(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return OM.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            // 파싱 실패 시 빈 맵 반환(클라이언트 안전성 우선)
            return Collections.emptyMap();
        }
    }
}
