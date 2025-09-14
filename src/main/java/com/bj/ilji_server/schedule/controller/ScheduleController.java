package com.bj.ilji_server.schedule.controller;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.service.ScheduleService;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    private User getAuthenticatedUser(User user) {
        if (user == null) {
            return userRepository.findById(4L)
                    .orElseThrow(() -> new RuntimeException("테스트 유저(ID=4)를 찾을 수 없습니다. DB를 확인해주세요."));
        }
        return user;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) List<Long> tagIds) { // tagIds 파라미터 추가
        User currentUser = getAuthenticatedUser(user);
        System.out.println(user);
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForUser(currentUser, tagIds);
        System.out.println(schedules);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@AuthenticationPrincipal User user, @RequestBody ScheduleCreateRequest request) {
        User currentUser = getAuthenticatedUser(user);
        ScheduleResponse newSchedule = scheduleService.createSchedule(currentUser, request);
        return ResponseEntity.ok(newSchedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@AuthenticationPrincipal User user, @PathVariable Long scheduleId, @RequestBody ScheduleUpdateRequest request) {
        User currentUser = getAuthenticatedUser(user);
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(currentUser, scheduleId, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@AuthenticationPrincipal User user, @PathVariable Long scheduleId) {
        User currentUser = getAuthenticatedUser(user);
        scheduleService.deleteSchedule(currentUser, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
