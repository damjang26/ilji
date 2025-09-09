package com.bj.ilji_server.ilog.service;

import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.friend.repository.FriendRepository;
import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogFeedResponseDto;
import com.bj.ilji_server.ilog.dto.ILogUpdateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ILogService {

    private final ILogRepository ilogRepository;
    // ✅ [추가] 의존성 주입: User 정보 조회, Firebase 연동, JSON 변환을 위해 추가합니다.
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FirebaseService firebaseService;
    private final ObjectMapper objectMapper;

    // 특정 사용자의 일기 목록 조회
    @Transactional(readOnly = true)
    public List<ILogResponse> getLogsForUser(User user) {
        // ✅ [개선] N+1 문제를 방지하기 위해 JOIN FETCH를 사용하는 새로운 Repository 메서드를 호출합니다.
        // ILog와 UserProfile을 한 번의 쿼리로 함께 조회하여 성능을 최적화합니다.
        List<ILog> logs = ilogRepository.findAllByUserProfileUserIdWithUserProfile(user.getUserProfile().getUserId());
        return logs.stream()
                .map(iLog -> ILogResponse.fromEntity(iLog, objectMapper))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ILogFeedResponseDto> getFeedForUser(User currentUser, int page, int size) {
        // ✅ [수정] pageable 객체를 먼저 생성해야 if문에서 사용할 수 있습니다.
        // 1. 최신순(createdAt 기준 내림차순)으로 정렬 조건을 설정한다.
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. 내가 팔로우하는 사람들의 ID 목록을 조회한다.
        // ✅ [수정] User ID가 아닌, UserProfile의 ID 목록을 가져와야 합니다.
        List<Long> followingProfileIds = friendRepository.findAllByFollower(currentUser)
                .stream()
                .map(friend -> friend.getFollowing().getUserProfile().getUserId())
                .collect(Collectors.toList());

        // 3. [개선] 만약 팔로우하는 사용자가 없다면, 빈 리스트를 전달하여 불필요한 쿼리 조건을 피합니다.
        // JPA와 대부분의 DB는 빈 리스트를 잘 처리하지만, 명시적으로 비어있음을 나타내는 것이 더 안전할 수 있습니다.
        // ✅ [개선] new ArrayList<>() 대신 Collections.emptyList()를 사용하여 불변의 빈 리스트를 명시적으로 사용합니다.
        if (followingProfileIds.isEmpty()) {
            return Page.empty(pageable); // 팔로우하는 사람이 없으면 비어있는 페이지를 즉시 반환하여 불필요한 DB 조회를 막습니다.
        }

        // 4. Repository에 위임하여 최종 피드 데이터를 조회한다.
        // ✅ [개선] N+1 문제를 방지하기 위해 JOIN FETCH를 사용하는 새로운 Repository 메서드를 호출합니다.
        Page<ILog> feedPage = ilogRepository.findFeedByUserProfileIdAndFollowingIds(
                currentUser.getUserProfile().getUserId(),
                followingProfileIds,
                ILog.Visibility.PUBLIC, // 다른 사람의 글은 '공개'만
                pageable
        );

        // 5. Page<ILog>를 Page<ILogResponse>로 변환하여 반환한다.
        return feedPage.map(iLog -> ILogFeedResponseDto.fromEntity(iLog, objectMapper));
    }

    // ✅ [수정] 일기 등록 메서드를 이미지 파일(MultipartFile)을 함께 처리하도록 변경합니다.
    @Transactional
    public ILogResponse createIlog(ILogCreateRequest request, List<MultipartFile> images) throws IOException {
        // 1. Firebase에 이미지 업로드 및 URL 생성
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                // FirebaseService를 사용해 이미지를 업로드하고 URL을 받습니다.
                String imageUrl = firebaseService.uploadFile(image, "ilog");
                imageUrls.add(imageUrl);
            }
        }

        // 2. 이미지 URL 리스트를 JSON 문자열로 변환
        String imgUrlJson = objectMapper.writeValueAsString(imageUrls);

        // 3. 요청한 사용자를 찾습니다.
        User user = userRepository.findById(request.getWriterId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getWriterId()));

        // 4. ILog 엔티티를 생성합니다. User가 아닌 UserProfile을 저장합니다.
        ILog newIlog = ILog.builder()
                .userProfile(user.getUserProfile())
                .logDate(request.getLogDate())
                .content(request.getContent())
                .imgUrl(imgUrlJson)
                .visibility(request.getVisibility())
                // ✅ [수정] 누락되었던 태그 정보를 빌더에 추가합니다.
                .friendTags(request.getFriendTags())
                .tags(request.getTags())
                .build();

        ILog savedIlog = ilogRepository.save(newIlog);

        // 5. 저장된 Entity를 Response DTO로 변환하여 반환
        return ILogResponse.fromEntity(savedIlog, objectMapper);
    }

    // 특정 날짜 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getLogByDate(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findByUserProfileUserIdAndLogDate(user.getUserProfile().getUserId(), date)
                .map(log -> ILogResponse.fromEntity(log, objectMapper))
                .orElse(null);
    }

    // 이전 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getPreviousLog(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findFirstByUserProfileUserIdAndLogDateLessThanOrderByLogDateDesc(user.getUserProfile().getUserId(), date)
                .map(log -> ILogResponse.fromEntity(log, objectMapper))
                .orElse(null);
    }

    // 다음 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getNextLog(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findFirstByUserProfileUserIdAndLogDateGreaterThanOrderByLogDateAsc(user.getUserProfile().getUserId(), date)
                .map(log -> ILogResponse.fromEntity(log, objectMapper))
                .orElse(null);
    }

    // 일기 삭제
    @Transactional
    public void deleteLog(User user, Long logId) {
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기를 찾을 수 없습니다. id=" + logId));

        // ✅ [수정] 소유권 검사를 UserProfile의 User ID와 현재 로그인한 User의 ID를 비교합니다.
        // [수정] @MapsId 관계로 인해 userProfile.getUserId()가 null일 수 있으므로,
        // userProfile에 연결된 User 객체의 ID를 통해 비교해야 정확합니다.
        if (!log.getUserProfile().getUser().getId().equals(user.getId())) {
            throw new SecurityException("일기를 삭제할 권한이 없습니다.");
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

        // [수정] @MapsId 관계로 인해 userProfile.getUserId()가 null일 수 있으므로,
        // userProfile에 연결된 User 객체의 ID를 통해 비교해야 정확합니다.
        if (!log.getUserProfile().getUser().getId().equals(user.getId())) {
            // ✅ [개선] 권한 없음 예외는 SecurityException을 사용하는 것이 더 의미에 맞습니다.
            throw new SecurityException("일기를 수정할 권한이 없습니다.");
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
        // ✅ [수정] fromEntity 메서드에 ObjectMapper를 전달하여 JSON 필드를 올바르게 처리하도록 수정합니다.
        return ILogResponse.fromEntity(log, objectMapper);
    }

}
