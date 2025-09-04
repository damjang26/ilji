package com.bj.ilji_server.schedule.entity;

import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "calendar_id", nullable = false)
    private Long calendarId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(nullable = false)
    private String title;

    private String location;

    @Lob
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_all_day", nullable = false)
    private Integer isAllDay; // DB의 NUMBER(1) 타입과 일치시키기 위해 Integer로 변경

    private String rrule;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Schedule(Long userId, Long calendarId, String title, String location, Tag tag, String description, LocalDateTime startTime, LocalDateTime endTime, Integer isAllDay, String rrule) {
        this.userId = userId;
        this.calendarId = calendarId;
        this.title = title;
        this.location = location;
        this.tag = tag;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAllDay = isAllDay;
        this.rrule = rrule;
    }

    public void update(ScheduleUpdateRequest request, Tag tag) {
        this.calendarId = request.getCalendarId();
        this.title = request.getTitle();
        this.location = request.getLocation();
        this.tag = tag;
        this.description = request.getDescription();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        // DTO의 boolean 값을 1 또는 0으로 변환하여 저장
        this.isAllDay = request.isAllDay() ? 1 : 0;
        this.rrule = request.getRrule();
    }
}
