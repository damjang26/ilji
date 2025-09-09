package com.bj.ilji_server.schedule.repository;

import com.bj.ilji_server.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    // 여러 태그 ID에 해당하는 스케줄을 찾는 메서드
    List<Schedule> findByTag_IdInOrderByStartTimeAsc(List<Long> tagIds);
}
