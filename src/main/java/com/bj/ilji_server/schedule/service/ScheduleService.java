package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.repository.TagRepository;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import java.text.ParseException;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TagRepository tagRepository;

    /**
     * 캘린더 뷰를 위해 특정 기간 내의 모든 일정(반복 일정 포함)을 조회합니다.
     *
     * @param user        사용자
     * @param periodStart 조회 시작일
     * @param periodEnd   조회 종료일
     * @return 기간 내의 모든 일정이 펼쳐진 리스트
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesForPeriod(User user, LocalDate periodStart, LocalDate periodEnd) {
        // 1. 사용자의 모든 일정을 DB에서 가져옵니다. (성능 최적화가 필요하다면 기간으로 1차 필터링)
        List<Schedule> allSchedules = scheduleRepository.findByUserIdOrderByStartTimeAsc(user.getId());

        List<ScheduleResponse> resultEvents = new ArrayList<>();

        // 2. 각 일정을 순회하며 기간 내에 포함되는지 확인합니다.
        for (Schedule schedule : allSchedules) {
            if (StringUtils.hasText(schedule.getRrule())) {
                // 2-1. 반복 일정인 경우, ical4j로 펼쳐서 결과에 추가합니다.
                resultEvents.addAll(expandRecurringSchedule(schedule, periodStart, periodEnd));
            } else {
                // 2-2. 반복이 아닌 단일 일정인 경우
                LocalDate scheduleStart = schedule.getStartTime().toLocalDate();
                LocalDate scheduleEnd = schedule.getEndTime().toLocalDate();

                // 단일 일정이 조회 기간과 겹치는지 확인
                if (!scheduleStart.isAfter(periodEnd) && !scheduleEnd.isBefore(periodStart)) {
                    resultEvents.add(new ScheduleResponse(schedule));
                }
            }
        }
        return resultEvents;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesForUser(User user, List<Long> tagIds) {
        List<Schedule> schedules;
        if (CollectionUtils.isEmpty(tagIds)) {
            schedules = scheduleRepository.findByUserIdOrderByStartTimeAsc(user.getId());
        } else {



            schedules = scheduleRepository.findByUserIdAndTagIdInOrderByStartTimeAsc(user.getId(), tagIds);
        }
        return schedules.stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(User user, ScheduleCreateRequest request) {
        Tag tag = findTagById(request.getTagId());

        Schedule newSchedule = Schedule.builder()
                .userId(user.getId())
                .calendarId(request.getCalendarId())
                .title(request.getTitle())
                .location(request.getLocation())
                .tag(tag)
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                // DTO의 boolean 값을 1 또는 0으로 변환하여 저장
                .isAllDay(request.isAllDay() ? 1 : 0)
                .rrule(request.getRrule())
                .build();

        Schedule savedSchedule = scheduleRepository.save(newSchedule);
        return new ScheduleResponse(savedSchedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(User user, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + scheduleId));

        if (!schedule.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("이 일정을 수정할 권한이 없습니다.");
        }

        Tag tag = findTagById(request.getTagId());
        schedule.update(request, tag);

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(User user, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + scheduleId));

        if (!schedule.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("이 일정을 삭제할 권한이 없습니다.");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    private Tag findTagById(Long tagId) {
        if (tagId == null) {
            return null;
        }
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다. id=" + tagId));
    }

    /**
     * ical4j를 사용하여 반복 일정을 특정 기간 내의 개별 일정으로 확장(expand)합니다.
     *
     * @param schedule      반복 규칙(rrule)을 가진 원본 일정
     * @param periodStart   조회 시작일
     * @param periodEnd     조회 종료일
     * @return 확장된 개별 일정(ScheduleResponse)의 리스트
     */
    private List<ScheduleResponse> expandRecurringSchedule(
            Schedule schedule, LocalDate periodStart, LocalDate periodEnd) {
        try {
            RRule<LocalDateTime> rrule = new RRule<>(schedule.getRrule());

            ZoneId zone = ZoneId.systemDefault();

            VEvent prototype = new VEvent(schedule.getStartTime(), schedule.getEndTime(), schedule.getTitle());
            prototype.getProperties().add(rrule);

            // 조회 구간은 ZonedDateTime으로
            Period<java.time.ZonedDateTime> searchPeriod = new Period<>(
                    periodStart.atStartOfDay(zone),
                    periodEnd.plusDays(1).atStartOfDay(zone)
            );

            // calculateRecurrenceSet의 제네릭이 깨져서 Period<?>로 올 수 있으니, 변환 유틸로 흡수
            return prototype.calculateRecurrenceSet(searchPeriod).stream()
                    .map(p -> new ScheduleResponse(
                            schedule,
                            temporalToLocalDateTime(p.getStart(), zone)
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 RRULE 형식입니다: " + schedule.getRrule(), e);
        }
    }

    /** Period<?>.getStart()가 무엇이든 LocalDateTime으로 변환 */
    private static LocalDateTime temporalToLocalDateTime(java.time.temporal.Temporal t, ZoneId zone) {
        if (t instanceof java.time.ZonedDateTime zdt) return zdt.toLocalDateTime();
        if (t instanceof java.time.LocalDateTime ldt) return ldt;
        if (t instanceof java.time.Instant inst) return LocalDateTime.ofInstant(inst, zone);
        if (t instanceof java.time.LocalDate ld) return ld.atStartOfDay(); // 종일 이벤트

        throw new IllegalArgumentException("Unsupported temporal type: " + t.getClass());
    }


}
