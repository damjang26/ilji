package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.schedule.dto.ScheduleBrief;
import com.bj.ilji_server.schedule.entity.Schedule;
import com.bj.ilji_server.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service                       // ← 스프링 빈 등록 (ScheduleReader 인터페이스 구현체)
@RequiredArgsConstructor       // ← 생성자 주입(Repository 주입)
public class ScheduleReaderImpl implements ScheduleReader {

    private final ScheduleRepository scheduleRepository;

    /**
     * 특정 유저의 '해당 날짜'와 겹치는 일정 상단 N개를 간단 DTO로 반환
     * - 날짜 경계: [date 00:00, date+1 00:00) (포함/제외)
     * - 정렬/자르기: Repository 쿼리에서 ASC 정렬, PageRequest.of(0, limit)
     */
    @Override
    public List<ScheduleBrief> getSchedulesForDate(Long userId, LocalDate date, int limit) {
        int size = Math.max(1, limit);                 // 방어적: 0/음수 들어와도 최소 1
        LocalDateTime start = date.atStartOfDay();     // yyyy-MM-dd 00:00
        LocalDateTime end   = date.plusDays(1).atStartOfDay(); // 다음날 00:00

        var list = scheduleRepository.findTopByUserAndDay(
                userId, start, end, PageRequest.of(0, size));

        // 엔티티 → 얇은 요약 DTO로 변환 (알림/요약에 필요한 최소 필드만)
        return list.stream().map(this::toBrief).toList();
    }

    /**
     * 특정 유저의 '해당 날짜'와 겹치는 일정 총 개수
     * - 스케줄러에서 "0이면 알림 스킵"에 사용
     */
    @Override
    public int countSchedulesForDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.plusDays(1).atStartOfDay();
        return scheduleRepository.countByUserAndDay(userId, start, end);
    }

    /**
     * 엔티티 → 알림/요약 표시용 DTO 매핑
     * - 링크 경로는 프로젝트 라우팅 규칙에 맞게 조정하세요.
     * - isAllDay가 필요한 경우, starts/ends를 null로 처리하거나 별도 표기를 원하면 이 부분에서 가공
     */
    private ScheduleBrief toBrief(Schedule s) {
        String link = "/schedules/" + s.getId();  // TODO: 실제 상세 경로 규칙에 맞추어 수정
        return new ScheduleBrief(
                s.getId(),
                s.getTitle(),
                s.getStartTime(),
                s.getEndTime(),
                link
        );
    }
}
