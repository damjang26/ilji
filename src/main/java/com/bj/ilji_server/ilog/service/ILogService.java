package com.bj.ilji_server.ilog.service;

import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogUpdateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
                String imageUrl = firebaseService.uploadFile(image, "ilog");
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

        // ✅ 이미지 삭제 (실패 시 예외 → 트랜잭션 롤백)
        if (log.getImgUrl() != null && !log.getImgUrl().isBlank()) {
            try {
                List<String> imageUrls = objectMapper.readValue(
                        log.getImgUrl(),
                        new TypeReference<List<String>>() {}
                );

                for (String url : imageUrls) {
                    firebaseService.deleteFile(url); // 이제 실패하면 IOException 던짐
                }
            } catch (Exception e) {
                throw new RuntimeException("이미지 삭제에 실패했습니다. 일기 삭제를 중단합니다.", e);
            }
        }

        // ✅ 모든 이미지 삭제 성공 후 DB 삭제
        ilogRepository.deleteById(logId);
    }


    // 일기 수정
    @Transactional
    public ILogResponse updateLog(Long logId, User user, ILogUpdateRequest request, List<MultipartFile> newImages) throws IOException {
        // 1. 일기 조회 및 수정 권한 확인
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기를 찾을 수 없습니다. id=" + logId));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 2. 이미지 변경 처리
        // 2-1. DB에 저장된 기존 이미지 URL 목록을 가져옵니다.
        List<String> oldImageUrls = new ArrayList<>();
        if (log.getImgUrl() != null && !log.getImgUrl().isBlank()) {
            oldImageUrls = objectMapper.readValue(log.getImgUrl(), new TypeReference<>() {});
        }

        // 2-2. 프론트에서 보낸 '유지할 이미지' 목록에 없는 기존 이미지는 Firebase에서 삭제합니다.
        List<String> existingUrlsToKeep = request.getExistingImageUrls() != null ? request.getExistingImageUrls() : new ArrayList<>();

        List<String> urlsToDelete = oldImageUrls.stream()
                .filter(oldUrl -> !existingUrlsToKeep.contains(oldUrl))
                .collect(Collectors.toList());

        for (String url : urlsToDelete) {
            try {
                firebaseService.deleteFile(url);
            } catch (Exception e) {
                throw new RuntimeException("기존 이미지 삭제에 실패했습니다. 일기 수정을 중단합니다.", e);
            }
        }

        // 2-3. 새로 첨부된 이미지가 있다면 Firebase에 업로드합니다.
        List<String> newUploadedUrls = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                String imageUrl = firebaseService.uploadFile(image, "ilog");
                newUploadedUrls.add(imageUrl);
            }
        }

        // 2-4. 최종 이미지 목록을 구성하고 JSON 문자열로 변환합니다.
        List<String> finalImageUrls = new ArrayList<>(existingUrlsToKeep);
        finalImageUrls.addAll(newUploadedUrls);
        String finalImageUrlsJson = objectMapper.writeValueAsString(finalImageUrls);

        // 3. 엔티티의 내용을 업데이트합니다.
        log.update(request.getContent(), finalImageUrlsJson, request.getVisibility());

        // 4. 변경된 엔티티를 Response DTO로 변환하여 반환합니다. (@Transactional에 의해 DB에는 자동 저장됩니다.)
        return ILogResponse.fromEntity(log);
    }

}
