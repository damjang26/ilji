package com.bj.ilji_server.schedule.dto;

import com.bj.ilji_server.schedule.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleResponse {

    private final Long id;
    private final Long userId;
    private final Long calendarId;
    private final String title;
    private final String location;
    private final Long tagId;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final boolean isAllDay;
    private final String rrule;
    private final LocalDateTime createdAt;

    public ScheduleResponse(Schedule entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.calendarId = entity.getCalendarId();
        this.title = entity.getTitle();
        this.location = entity.getLocation();
        this.tagId = entity.getTag() != null ? entity.getTag().getId() : null;
        this.description = entity.getDescription();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        // [수정] Entity의 Integer(0/1) 값을 DTO의 boolean 값으로 변환합니다.
        this.isAllDay = entity.getIsAllDay() != null && entity.getIsAllDay() == 1;
        this.rrule = entity.getRrule();
        this.createdAt = entity.getCreatedAt();
    }
}
