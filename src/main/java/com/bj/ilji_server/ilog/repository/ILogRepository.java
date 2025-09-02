package com.bj.ilji_server.ilog.repository;

import com.bj.ilji_server.ilog.entity.ILog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ILogRepository extends JpaRepository<ILog, Long>  {

    // userId 기준으로 일기 목록 찾기 (날짜 오름차순 정렬)
    List<ILog> findByUserIdOrderByIlogDateAsc(Long userId);

    // 특정 날짜 일기 찾기
    ILog findByUserIdAndIlogDate(Long userId, LocalDate ilogDate);

    // 이전 일기 (현재 날짜보다 작은 것 중 제일 최근 것 하나)
    ILog findFirstByUserIdAndIlogDateLessThanOrderByIlogDateDesc(Long userId, LocalDate ilogDate);

    // 다음 일기 (현재 날짜보다 큰 것 중 제일 가까운 것 하나)
    ILog findFirstByUserIdAndIlogDateGreaterThanOrderByIlogDateAsc(Long userId, LocalDate ilogDate);
}
