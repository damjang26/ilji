package com.bj.ilji_server.schedule.repository;

import com.bj.ilji_server.schedule.entity.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // (기존) 유저의 모든 일정 시작시각 오름차순
    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    // (기존) 특정 태그 집합에 속한 일정
    List<Schedule> findByUserIdAndTagIdInOrderByStartTimeAsc(Long userId, List<Long> tagIds);

    /**
     * ✅ 특정 '하루'와 겹치는 일정 상단 N개를 조회 (시작시각 오름차순)
     *
     * 겹침 조건(Overlap):
     *   s.startTime < :end   &&   s.endTime >= :start
     *
     * 즉, 일정의 시작이 그날의 끝 이전이고,
     * 일정의 종료가 그날의 시작 이상이면 "그 하루와 겹친다"로 판단합니다.
     * (종일/자정 경계 넘는 이벤트도 포함)
     *
     * 정렬: 시작시각 ASC
     * 페이징: Pageable(size=N)로 상단 N개만 자르기
     */
    @Query("""
        SELECT s FROM Schedule s
         WHERE s.userId = :userId
           AND s.startTime < :end
           AND s.endTime   >= :start      
         ORDER BY s.startTime ASC
    """)
//               -- 시작이 그날의 끝 이전
//           -- 종료가 그날의 시작 이상

    List<Schedule> findTopByUserAndDay(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,   // 하루 시작(포함)
            @Param("end")   LocalDateTime end,     // 다음날 00:00(제외)
            Pageable pageable                      // PageRequest.of(0, limit)
    );

    /**
     * ✅ 특정 '하루'와 겹치는 일정 개수 카운트
     * (위와 동일한 겹침 조건)
     */
    @Query("""
        SELECT COUNT(s) FROM Schedule s
         WHERE s.userId = :userId
           AND s.startTime < :end
           AND s.endTime   >= :start
    """)
    int countByUserAndDay(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );
}
