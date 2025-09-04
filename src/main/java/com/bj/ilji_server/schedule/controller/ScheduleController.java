package com.bj.ilji_server.schedule.controller;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.service.ScheduleService;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 캘린더 뷰를 위한 기간별 일정 조회 (반복 포함)
     * GET /api/schedules/period?start=2024-09-01&end=2024-09-30
     */
    @GetMapping("/period")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesForPeriod(
            @AuthenticationPrincipal User user,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForPeriod(user, start, end);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 리스트 뷰를 위한 태그별 일정 조회 (반복 미포함)
     * GET /api/schedules?tagIds=1,2,3
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedulesForUser(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForUser(user, tagIds);
        System.out.println(schedules);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@AuthenticationPrincipal User user, @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse createdSchedule = scheduleService.createSchedule(user, request);
        // 생성 성공 시에는 201 Created 상태 코드를 반환하는 것이 표준적입니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@AuthenticationPrincipal User user, @PathVariable Long scheduleId, @RequestBody ScheduleUpdateRequest request) {
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(user, scheduleId, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@AuthenticationPrincipal User user, @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(user, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
