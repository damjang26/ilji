package com.bj.ilji_server.ilog.controller;

import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.service.ILogService;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/i-log")
@RequiredArgsConstructor
public class ILogController {

    private final ILogService ilogService;

    // ---------------------------------------------------
    // 1️⃣ 내 모든 일기 조회 (날짜 오름차순)
    // ---------------------------------------------------
    @GetMapping
    public ResponseEntity<List<ILogResponse>> getMyLogs(@AuthenticationPrincipal User user) {
        List<ILogResponse> logs = ilogService.getLogsForUser(user);
        return ResponseEntity.ok(logs);
    }

    // ---------------------------------------------------
    // 2️⃣ 일기 등록
    // ---------------------------------------------------
    @PostMapping
    public ResponseEntity<ILogResponse> createLog(
            @AuthenticationPrincipal User user,
            @RequestBody ILogCreateRequest requestDto) {

        ILogResponse newLog = ilogService.createLog(user, requestDto);
        return ResponseEntity.ok(newLog);
    }

    // ---------------------------------------------------
    // 3️⃣ 특정 날짜 일기 조회
    // ---------------------------------------------------
    @GetMapping("/date/{date}")
    public ResponseEntity<ILogResponse> getLogByDate(
            @AuthenticationPrincipal User user,
            @PathVariable("date") String dateStr) {

        LocalDate date = LocalDate.parse(dateStr); // "YYYY-MM-DD" 형식 가정
        ILogResponse log = ilogService.getLogByDate(user, date);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }

    // ---------------------------------------------------
    // 4️⃣ 이전 일기 조회
    // ---------------------------------------------------
    @GetMapping("/previous/{date}")
    public ResponseEntity<ILogResponse> getPreviousLog(
            @AuthenticationPrincipal User user,
            @PathVariable("date") String dateStr) {

        LocalDate date = LocalDate.parse(dateStr);
        ILogResponse log = ilogService.getPreviousLog(user, date);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }

    // ---------------------------------------------------
    // 5️⃣ 다음 일기 조회
    // ---------------------------------------------------
    @GetMapping("/next/{date}")
    public ResponseEntity<ILogResponse> getNextLog(
            @AuthenticationPrincipal User user,
            @PathVariable("date") String dateStr) {

        LocalDate date = LocalDate.parse(dateStr);
        ILogResponse log = ilogService.getNextLog(user, date);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }

    // ---------------------------------------------------
    // 6️⃣ 일기 삭제
    // ---------------------------------------------------
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @AuthenticationPrincipal User user,
            @PathVariable("logId") Long logId) {

        ilogService.deleteLog(user, logId);
        return ResponseEntity.noContent().build();
    }
}
