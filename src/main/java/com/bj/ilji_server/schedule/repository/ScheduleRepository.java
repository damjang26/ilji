package com.bj.ilji_server.schedule.repository;

import com.bj.ilji_server.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 'userId'를 기준으로 모든 일정을 찾는 메소드를 자동으로 생성해줍니다.
    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);
}