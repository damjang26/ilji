package com.bj.ilji_server.ilog.service;

import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ILogService {

    private final ILogRepository ilogRepository;
    // ✅ [추가] 의존성 주입: User 정보 조회, Firebase 연동, JSON 변환을 위해 추가합니다.
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;
    private final ObjectMapper objectMapper;

    // 특정 사용자의 일기 목록 조회
    @Transactional(readOnly = true)
    public List<ILogResponse> getLogsForUser(User user) {
        List<ILog> logs = ilogRepository.findByUserIdOrderByIlogDateAsc(user.getId());
        return logs.stream()
                .map(ILogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ [수정] 일기 등록 메서드를 이미지 파일(MultipartFile)을 함께 처리하도록 변경합니다.
    @Transactional
    public ILogResponse createIlog(ILogCreateRequest request, List<MultipartFile> images) throws IOException {
        // 1. 이미지 업로드 및 URL 생성
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                // FirebaseService를 사용해 이미지를 업로드하고 URL을 받습니다.
                String imageUrl = firebaseService.uploadFile(image, "ilog-images");
                imageUrls.add(imageUrl);
            }
        }

        // 2. 이미지 URL 리스트를 JSON 문자열로 변환하여 DTO에 설정
        String imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
        request.setImgUrl(imageUrlsJson);

        // 3. DTO를 Entity로 변환하여 DB에 저장
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        ILog newIlog = request.toEntity(user);
        ILog savedIlog = ilogRepository.save(newIlog);

        // 4. 저장된 Entity를 Response DTO로 변환하여 반환
        return ILogResponse.fromEntity(savedIlog);
    }

    // 특정 날짜 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getLogByDate(User user, LocalDate date) {
        ILog log = ilogRepository.findByUserIdAndIlogDate(user.getId(), date);
        if (log == null) {
            return null;
        }
        return ILogResponse.fromEntity(log);
    }

    // 이전 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getPreviousLog(User user, LocalDate date) {
        ILog log = ilogRepository.findFirstByUserIdAndIlogDateLessThanOrderByIlogDateDesc(user.getId(), date);
        if (log == null) {
            return null;
        }
        return ILogResponse.fromEntity(log);
    }

    // 다음 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getNextLog(User user, LocalDate date) {
        ILog log = ilogRepository.findFirstByUserIdAndIlogDateGreaterThanOrderByIlogDateAsc(user.getId(), date);
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
