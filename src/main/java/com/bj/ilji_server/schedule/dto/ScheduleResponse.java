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
    private final String tags;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final boolean isAllDay;
    private final String rrule;
    private final LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 생성자
    public ScheduleResponse(Schedule entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.calendarId = entity.getCalendarId();
        this.title = entity.getTitle();
        this.location = entity.getLocation();
        this.tags = entity.getTags();
        this.description = entity.getDescription();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        this.isAllDay = entity.isAllDay();
        this.rrule = entity.getRrule();
        this.createdAt = entity.getCreatedAt();
    }
}