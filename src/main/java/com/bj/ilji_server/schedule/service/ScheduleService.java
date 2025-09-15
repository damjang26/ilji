package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.schedule.dto.ScheduleBrief;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.dto.TagUpdateRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService implements ScheduleReader {

    private final ScheduleRepository scheduleRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleBrief> getSchedulesForDate(Long userId, LocalDate date, int limit) {
        return scheduleRepository.findTopNSchedulesByUserIdAndDate(userId, date)
                .stream()
                .map(ScheduleBrief::new)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int countSchedulesForDate(Long userId, LocalDate date) {
        return scheduleRepository.countByUserIdAndDate(userId, date);
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

    @Transactional
    public TagResponse updateTag(User user, Long tagId, TagUpdateRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 태그를 수정할 권한이 없습니다.");
        }

        tag.update(request);

        return new TagResponse(tag);
    }
}
