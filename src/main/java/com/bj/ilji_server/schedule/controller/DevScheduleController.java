// C:/LGE/ilji_server/src/main/java/com/bj/ilji_server/schedule/controller/DevScheduleController.java

package com.bj.ilji_server.schedule.controller;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.service.ScheduleService;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.user.repository.UserRepository;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev/schedules") // 개발용 전용 주소로 변경
@RequiredArgsConstructor
public class DevScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository; // 테스트 유저를 찾기 위해 주입

    // 항상 ID가 4인 테스트 유저를 반환하는 헬퍼 메서드
    private User getTestUser() {
        return userRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("테스트 유저(ID=4)를 찾을 수 없습니다. DB를 확인해주세요."));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(
            @RequestParam(required = false) List<Long> tagIds) {
        User testUser = getTestUser(); // 테스트 유저 가져오기
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForUser(testUser, tagIds);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@RequestBody ScheduleCreateRequest request) {
        User testUser = getTestUser(); // 테스트 유저 가져오기
        ScheduleResponse newSchedule = scheduleService.createSchedule(testUser, request);
        return ResponseEntity.ok(newSchedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleUpdateRequest request) {
        User testUser = getTestUser(); // 테스트 유저 가져오기
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(testUser, scheduleId, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        User testUser = getTestUser(); // 테스트 유저 가져오기
        scheduleService.deleteSchedule(testUser, scheduleId);
        return ResponseEntity.noContent().build();
    }
}