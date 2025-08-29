package com.bj.ilji_server.schedule.controller;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.service.ScheduleService;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) List<Long> tagIds) { // tagIds 파라미터 추가
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForUser(user, tagIds);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@AuthenticationPrincipal User user, @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse newSchedule = scheduleService.createSchedule(user, request);
        return ResponseEntity.ok(newSchedule);
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
