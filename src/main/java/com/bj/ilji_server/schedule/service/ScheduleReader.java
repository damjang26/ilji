package com.bj.ilji_server.schedule.service;

import com.bj.ilji_server.schedule.dto.ScheduleBrief;
import java.time.LocalDate;
import java.util.List;

/**
 * 알림/대시보드 등이 일정 데이터를 읽을 때 사용하는 '공식 읽기 계약(Port)'.
 * 구현은 schedule 도메인 내부에서 제공합니다.
 */
public interface ScheduleReader {

    /** 특정 사용자의 특정 날짜 일정 상위 N개(표시용) */
    List<ScheduleBrief> getSchedulesForDate(Long userId, LocalDate date, int limit);

    /** 특정 사용자의 특정 날짜 전체 일정 개수 */
    int countSchedulesForDate(Long userId, LocalDate date);
}
