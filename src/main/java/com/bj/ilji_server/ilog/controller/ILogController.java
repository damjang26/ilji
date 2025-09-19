package com.bj.ilji_server.ilog.controller;

import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogFeedResponseDto;
import com.bj.ilji_server.ilog.dto.ILogUpdateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.service.ILogService;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/i-log")
@RequiredArgsConstructor
public class ILogController {

    private final ILogService ilogService;

    // ---------------------------------------------------
    // 🆕 소셜 피드 조회 (페이징)
    // ---------------------------------------------------
    @GetMapping("/feed")
    public ResponseEntity<Page<ILogFeedResponseDto>> getFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (user == null) {
            return ResponseEntity.status(401).build(); // 로그인하지 않은 사용자 접근 차단
        }

        Page<ILogFeedResponseDto> feedPage = ilogService.getFeedForUser(user, page, size);
        return ResponseEntity.ok(feedPage);
    }


    // ---------------------------------------------------
    // 1️⃣ 내 모든 일기 조회 (날짜 오름차순)
    // ---------------------------------------------------
    @GetMapping("/{logId}")
    public ResponseEntity<ILogResponse> getLogById(
            @PathVariable Long logId,
            @AuthenticationPrincipal User currentUser) {
        ILogResponse log = ilogService.getLogById(logId, currentUser);
        return ResponseEntity.ok(log);
    }

    @GetMapping
    public ResponseEntity<List<ILogResponse>> getMyLogs(@AuthenticationPrincipal User user) {
        List<ILogResponse> logs = ilogService.getLogsForUser(user);
        return ResponseEntity.ok(logs);
    }

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자의 모든 일기 조회 (친구 마이페이지용)
    // ---------------------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ILogResponse>> getLogsByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser // ✅ [추가] 현재 로그인한 사용자 정보를 가져옵니다.
    ) {
        // ✅ [수정] 서비스에 조회 대상 ID(userId)와 현재 사용자 정보(currentUser)를 함께 전달합니다.
        List<ILogResponse> logs = ilogService.getLogsByUserId(userId, currentUser);
        return ResponseEntity.ok(logs);
    }

    // ---------------------------------------------------
    //  특정 사용자가 '좋아요' 누른 일기 목록 조회
    // ---------------------------------------------------
    /**
     * 특정 사용자가 '좋아요'를 누른 일기 목록을 조회합니다.
     * @param userId 조회할 사용자의 ID. null일 경우 현재 로그인한 사용자를 대상으로 합니다.
     * @param sortBy 정렬 기준 ('liked_at': 좋아요 누른 순, 'uploaded_at': 일기 작성 순, 'popular': 인기순)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param currentUser 현재 로그인한 사용자 정보
     * @return '좋아요' 누른 일기 목록 (페이징 처리)
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<ILogFeedResponseDto>> getLikedILogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "liked_at") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {

        // 조회 대상 사용자의 ID를 결정합니다 (파라미터가 없으면 현재 사용자).
        Long targetUserId = (userId == null) ? currentUser.getId() : userId;

        Page<ILogFeedResponseDto> likedILogs = ilogService.getLikedILogsByUser(targetUserId, currentUser, sortBy, page, size);
        return ResponseEntity.ok(likedILogs);
    }

    // ---------------------------------------------------
    // 2️⃣ 일기 등록
    // ---------------------------------------------------
    // ✅ [수정] 이미지(Multipart)와 일기 데이터(JSON)를 함께 받도록 변경
    // ✅ [개선] 클라이언트는 항상 multipart/form-data로 요청하므로, 소비 타입을 하나로 명확히 합니다.
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ILogResponse> createIlog(
            // "images"라는 이름의 파일 파트를 받습니다. 파일이 없어도 오류가 나지 않도록 required = false 설정
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            // "request"라는 이름의 JSON 데이터 파트를 받습니다.
            @RequestPart("request") ILogCreateRequest request
    ) throws IOException {
        // 서비스 레이어에 이미지와 DTO를 모두 전달합니다.
        return ResponseEntity.ok(ilogService.createIlog(request, images));
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

    // ---------------------------------------------------
    // 7️⃣ 일기 수정
    // ---------------------------------------------------
    @PutMapping(value = "/{logId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ILogResponse> updateLog(
            @AuthenticationPrincipal User user,
            @PathVariable("logId") Long logId,
            // 새로 추가되거나 변경된 이미지 파일 목록
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages,
            // 수정될 텍스트 내용과 유지할 기존 이미지 URL 목록
            @RequestPart("request") ILogUpdateRequest request
    ) throws IOException {

        // 서비스 레이어에 수정에 필요한 모든 정보를 전달합니다.
        ILogResponse updatedLog = ilogService.updateLog(logId, user, request, newImages);
        return ResponseEntity.ok(updatedLog);
    }
}
