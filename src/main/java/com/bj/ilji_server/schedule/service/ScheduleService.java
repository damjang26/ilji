package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.entity.Schedule;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (의존성 주입)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true) // 데이터를 읽기만 하는 트랜잭션임을 명시 (성능 향상)
    public List<ScheduleResponse> getSchedulesByUserId(Long userId) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByStartTimeAsc(userId);
        return schedules.stream()
                .map(ScheduleResponse::new) // stream을 통해 각 Schedule Entity를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
        Schedule newSchedule = request.toEntity();
        Schedule savedSchedule = scheduleRepository.save(newSchedule);
        return new ScheduleResponse(savedSchedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + scheduleId));

        schedule.update(request);

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}