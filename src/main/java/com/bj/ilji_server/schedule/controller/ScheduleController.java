package com.bj.ilji_server.schedule.controller;

import com.bj.ilji_server.schedule.service.ScheduleService;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // 이 클래스가 REST API를 위한 Controller임을 나타냅니다.
@RequestMapping("/api/schedules") // 이 컨트롤러의 모든 요청은 /api/schedules 로 시작합니다.
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable Long userId) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByUserId(userId);
        return ResponseEntity.ok(schedules);
    }
}