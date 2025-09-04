package com.bj.ilji_server.schedule.dto;

import com.bj.ilji_server.schedule.entity.Schedule;
import lombok.Getter;

import java.time.Duration;
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

    // 기존 생성자: DB에 저장된 단일 일정을 변환할 때 사용합니다.
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
        this.isAllDay = entity.getIsAllDay() != null && entity.getIsAllDay() == 1;
        this.rrule = entity.getRrule();
        this.createdAt = entity.getCreatedAt();
    }

    /**
     * 반복 일정 확장용 생성자
     * 원본 일정의 정보는 그대로 복사하고, 시작/종료 시간만 새로 계산하여 설정합니다.
     *
     * @param originalSchedule    원본 Schedule 엔티티
     * @param occurrenceStartTime 이번에 계산된 반복 발생 시점의 시작 시간
     */
    public ScheduleResponse(Schedule originalSchedule, LocalDateTime occurrenceStartTime) {
        // 대부분의 정보는 원본에서 그대로 가져옵니다.
        this.id = originalSchedule.getId();
        this.userId = originalSchedule.getUserId();
        this.calendarId = originalSchedule.getCalendarId();
        this.title = originalSchedule.getTitle();
        this.location = originalSchedule.getLocation();
        this.tagId = (originalSchedule.getTag() != null) ? originalSchedule.getTag().getId() : null;
        this.description = originalSchedule.getDescription();
        this.isAllDay = originalSchedule.getIsAllDay() != null && originalSchedule.getIsAllDay() == 1;
        this.rrule = originalSchedule.getRrule();
        this.createdAt = originalSchedule.getCreatedAt();

        // 시작 시간과 종료 시간만 새로 계산합니다.
        // 1. 원본 이벤트의 기간(duration)을 계산합니다. (예: 1시간짜리 회의)
        Duration duration = Duration.between(originalSchedule.getStartTime(), originalSchedule.getEndTime());

        // 2. 이 가상 이벤트의 시작 시간을 설정합니다.
        this.startTime = occurrenceStartTime;
        // 3. 계산된 시작 시간에 기간을 더해 종료 시간을 설정합니다.
        this.endTime = occurrenceStartTime.plus(duration);
    }
}