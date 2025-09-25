package com.bj.ilji_server.ilog.mobile;

import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.dto.ILogUpdateRequest;
import com.bj.ilji_server.ilog.service.ILogService;
import com.bj.ilji_server.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mobile/i-log")
@RequiredArgsConstructor
public class MobileILogController {

    private final ILogService ilogService;
    private final ObjectMapper objectMapper;

    // [NEW] Create ILog
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ILogResponse> createIlog(
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart("request") String requestJson
    ) throws IOException {
        ILogCreateRequest request = objectMapper.readValue(requestJson, ILogCreateRequest.class);
        return ResponseEntity.ok(ilogService.createIlog(request, images));
    }

    // [NEW] Get all my logs
    @GetMapping
    public ResponseEntity<List<ILogResponse>> getMyLogs(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        List<ILogResponse> logs = ilogService.getLogsForUser(user);
        return ResponseEntity.ok(logs);
    }

    // [NEW] Get log by date
    @GetMapping("/date/{date}")
    public ResponseEntity<ILogResponse> getLogByDate(
            @AuthenticationPrincipal User user,
            @PathVariable("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        ILogResponse log = ilogService.getLogByDate(user, date);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }

    // [NEW] Get previous log
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

    // [NEW] Get next log
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

    // [NEW & FIXED] Update log
    @PutMapping(value = "/{logId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ILogResponse> updateLog(
            @AuthenticationPrincipal User user,
            @PathVariable("logId") Long logId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images, // Variable name changed to 'images'
            @RequestPart("request") String requestJson
    ) throws IOException {
        // Manual parsing
        ILogUpdateRequest request = objectMapper.readValue(requestJson, ILogUpdateRequest.class);
        // Pass the corrected variable to the service
        ILogResponse updatedLog = ilogService.updateLog(logId, user, request, images);
        return ResponseEntity.ok(updatedLog);
    }

    // [NEW] Delete log
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @AuthenticationPrincipal User user,
            @PathVariable("logId") Long logId) {
        ilogService.deleteLog(user, logId);
        return ResponseEntity.noContent().build();
    }
}
