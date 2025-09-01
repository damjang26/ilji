package com.bj.ilji_server.ilog.service;

import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ILogService {

    private final ILogRepository ilogRepository;

    // 특정 사용자의 일기 목록 조회
    @Transactional(readOnly = true)
    public List<ILogResponse> getLogsForUser(User user) {
        List<ILog> logs = ilogRepository.findByUserIdOrderByILogDateAsc(user.getId());
        return logs.stream()
                .map(ILogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 일기 등록
    @Transactional
    public ILogResponse createLog(User user, ILogCreateRequest requestDto) {
        ILog newLog = requestDto.toEntity(user);
        ILog savedLog = ilogRepository.save(newLog);
        return ILogResponse.fromEntity(savedLog);
    }

    // 특정 날짜 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getLogByDate(User user, LocalDate date) {
        ILog log = ilogRepository.findByUserIdAndILogDate(user.getId(), date);
        if (log == null) {
            return null;
        }
        return ILogResponse.fromEntity(log);
    }

    // 이전 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getPreviousLog(User user, LocalDate date) {
        ILog log = ilogRepository.findFirstByUserIdAndILogDateLessThanOrderByILogDateDesc(user.getId(), date);
        if (log == null) {
            return null;
        }
        return ILogResponse.fromEntity(log);
    }

    // 다음 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getNextLog(User user, LocalDate date) {
        ILog log = ilogRepository.findFirstByUserIdAndILogDateGreaterThanOrderByILogDateAsc(user.getId(), date);
        if (log == null) {
            return null;
        }
        return ILogResponse.fromEntity(log);
    }

    // 일기 삭제
    @Transactional
    public void deleteLog(User user, Long logId) {
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기를 찾을 수 없습니다. id=" + logId));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        ilogRepository.deleteById(logId);
    }
}
