package com.bj.ilji_server.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationListResponse {
    private List<NotificationItemDto> items;
    private long total;
    private int offset;
    private int limit;
}
