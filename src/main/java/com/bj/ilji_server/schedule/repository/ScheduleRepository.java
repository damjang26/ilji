package com.bj.ilji_server.schedule.repository;

import com.bj.ilji_server.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    // 여러 태그 ID에 해당하는 스케줄을 찾는 메서드
    List<Schedule> findByTag_IdInOrderByStartTimeAsc(List<Long> tagIds);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId " +
           "AND (" +
           "   (:includeNullTagId = true AND s.tag IS NULL) " +
           "   OR " +
           "   (s.tag.id IN :tagIds)" +
           ") " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findByUserIdAndTagIdsAndNullTagId(
        @Param("userId") Long userId,
        @Param("tagIds") List<Long> tagIds,
        @Param("includeNullTagId") boolean includeNullTagId
    );
}
