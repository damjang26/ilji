package com.bj.ilji_server.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleCreateRequest {

    private Long calendarId;
    private String title;
    private String location;
    private Long tagId; // "tags" -> "tagId"
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 'is' prefix는 Jackson에서 혼동을 일으킬 수 있으므로, 표준 Java Bean 명명 규칙(allDay)을 따릅니다.
    // @JsonProperty를 사용하여 JSON의 'isAllDay' 키와 명시적으로 매핑합니다.
    @JsonProperty("isAllDay")
    private boolean allDay;
    private String rrule;
}
