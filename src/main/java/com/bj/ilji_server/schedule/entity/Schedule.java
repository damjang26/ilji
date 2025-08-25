package com.bj.ilji_server.schedule.entity;

import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules") // 실제 데이터베이스 테이블 이름 지정
@Getter
@NoArgsConstructor // JPA는 기본 생성자를 필요로 합니다.
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle의 GENERATED AS IDENTITY와 매핑
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "calendar_id", nullable = false)
    private Long calendarId;

    @Column(nullable = false)
    private String title;

    private String location;

    private String tags;

    @Lob // CLOB 타입과 매핑
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_all_day", nullable = false)
    private boolean isAllDay; // NUMBER(1)은 boolean으로 매핑됩니다.

    private String rrule;

    @CreationTimestamp // Entity가 생성될 때 자동으로 현재 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Entity가 수정될 때 자동으로 현재 시간 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Schedule(Long userId, Long calendarId, String title, String location, String tags, String description, LocalDateTime startTime, LocalDateTime endTime, boolean isAllDay, String rrule) {
        this.userId = userId;
        this.calendarId = calendarId;
        this.title = title;
        this.location = location;
        this.tags = tags;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAllDay = isAllDay;
        this.rrule = rrule;
    }

    public void update(ScheduleUpdateRequest request) {
        this.calendarId = request.getCalendarId();
        this.title = request.getTitle();
        this.location = request.getLocation();
        this.tags = request.getTags();
        this.description = request.getDescription();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        this.isAllDay = request.isAllDay();
        this.rrule = request.getRrule();
    }
}