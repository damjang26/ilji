package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.schedule.dto.ScheduleCreateRequest;
import com.bj.ilji_server.schedule.dto.ScheduleUpdateRequest;
import com.bj.ilji_server.schedule.entity.Schedule;
import com.bj.ilji_server.schedule.dto.ScheduleResponse;
import com.bj.ilji_server.schedule.repository.ScheduleRepository;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (의존성 주입)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true) // 데이터를 읽기만 하는 트랜잭션임을 명시 (성능 향상)
    public List<ScheduleResponse> getSchedulesForUser(User user) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByStartTimeAsc(user.getId());
        return schedules.stream()
                .map(ScheduleResponse::new) // stream을 통해 각 Schedule Entity를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(User user, ScheduleCreateRequest request) {
        // DTO를 Entity로 변환하고, 인증된 사용자의 ID를 설정합니다.
        Schedule newSchedule = Schedule.builder()
                .userId(user.getId()) // ✅ 인증된 사용자의 ID를 직접 설정
                .calendarId(request.getCalendarId())
                .title(request.getTitle())
                .location(request.getLocation())
                .tags(request.getTags())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAllDay(request.isAllDay())
                .rrule(request.getRrule())
                .build();

        Schedule savedSchedule = scheduleRepository.save(newSchedule);
        return new ScheduleResponse(savedSchedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(User user, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + scheduleId));

        // ✅ 소유권 검증: 요청한 사용자가 일정의 소유자인지 확인합니다.
        if (!schedule.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("이 일정을 수정할 권한이 없습니다.");
        }

        schedule.update(request);

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(User user, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + scheduleId));

        // ✅ 소유권 검증: 요청한 사용자가 일정의 소유자인지 확인합니다.
        if (!schedule.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("이 일정을 삭제할 권한이 없습니다.");
        }
        scheduleRepository.deleteById(scheduleId);
    }
}