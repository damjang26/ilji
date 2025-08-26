package com.bj.ilji_server.schedule.dto;

import com.bj.ilji_server.schedule.entity.Schedule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {

    private Long calendarId;
    private String title;
    private String location;
    private String tags;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAllDay;
    private String rrule;

    public Schedule toEntity() {
        return Schedule.builder()
                .calendarId(this.calendarId)
                .title(this.title)
                .location(this.location)
                .tags(this.tags)
                .description(this.description)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .isAllDay(this.isAllDay)
                .rrule(this.rrule)
                .build();
    }
}