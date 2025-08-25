package com.bj.ilji_server.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleUpdateRequest {

    private Long calendarId;
    private String title;
    private String location;
    private String tags;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAllDay;
    private String rrule;
}