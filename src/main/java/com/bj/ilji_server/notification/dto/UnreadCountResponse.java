package com.bj.ilji_server.notification.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UnreadCountResponse {
    private long count;
}
