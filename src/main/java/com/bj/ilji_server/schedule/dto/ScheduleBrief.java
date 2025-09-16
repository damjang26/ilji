package com.bj.ilji_server.schedule.dto;

import java.time.LocalDateTime;

/** 일정 요약 DTO (다른 기능들이 공통으로 소비) */
public record ScheduleBrief(
        Long id,
        String title,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String linkUrl // 상세 페이지 라우팅(없으면 null 허용)
) {}
