package com.bj.ilji_server.schedule.repository;

import com.bj.ilji_server.schedule.entity.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bj.ilji_server.tag.entity.Tag;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    // 여러 태그 ID에 해당하는 스케줄을 찾는 메서드 추가
    List<Schedule> findByUserIdAndTagIdInOrderByStartTimeAsc(Long userId, List<Long> tagIds);

    // [추가] 특정 태그를 참조하는 모든 일정들의 tag 필드를 null로 변경하는 쿼리입니다.
    // 이 메서드는 태그를 삭제하기 전에 호출되어야 합니다.
    @Modifying
    @Query("UPDATE Schedule s SET s.tag = null WHERE s.tag = :tag")
    void disassociateTagFromSchedules(@Param("tag") Tag tag);

    List<Schedule> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    int countByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
