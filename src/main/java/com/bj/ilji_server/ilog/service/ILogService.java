package com.bj.ilji_server.ilog.service;


import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.friend.entity.Friend; // Import Friend entity
import com.bj.ilji_server.friend.repository.FriendRepository;
import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogFeedResponseDto;
import com.bj.ilji_server.ilog.dto.ILogUpdateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.ilog_comments.repository.IlogCommentRepository;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.notification.packing.NotificationComposer; // Import NotificationComposer
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ILogService {

    private final ILogRepository ilogRepository;
    private final UserRepository userRepository;
    private final IlogCommentRepository ilogCommentRepository;
    private final com.bj.ilji_server.ilog_comment_likes.repository.IlogCommentLikeRepository ilogCommentLikeRepository;
    private final com.bj.ilji_server.likes.repository.LikesRepository likesRepository;
    private final FriendRepository friendRepository;
    private final FirebaseService firebaseService;
    private final ObjectMapper objectMapper;
    private final NotificationComposer notificationComposer; // NotificationComposer 주입

    // 특정 사용자의 일기 목록 조회
    @Transactional(readOnly = true)
    public List<ILogResponse> getLogsForUser(User user) {
        // ✅ [개선] N+1 문제를 방지하기 위해 JOIN FETCH를 사용하여 ILog와 UserProfile을 한 번의 쿼리로 조회합니다.
        // ILog와 UserProfile을 한 번의 쿼리로 함께 조회하여 성능을 최적화합니다.
        List<ILog> logs = ilogRepository.findAllByUserProfileUserIdWithUserProfile(user.getUserProfile().getUserId());
        return logs.stream()
                .map(iLog -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
                    return ILogResponse.fromEntity(iLog, bestComment, objectMapper, user.getUserProfile().getUserId());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ILogResponse> getLogsForUserByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        // Repository를 호출하여 기간 내의 데이터를 조회합니다.
        // ✅ [수정] User 객체 대신 UserProfile의 ID를 전달합니다.
        List<ILog> logs = ilogRepository.findByUserProfileUserIdAndLogDateBetweenWithUserProfile(user.getUserProfile().getUserId(), startDate, endDate);

        // 조회된 ILog 엔티티 리스트를 ILogResponse DTO 리스트로 변환하여 반환합니다.
        // ✅ [수정] fromEntity 메소드가 여러 인자를 필요로 하므로, 메소드 참조 대신 람다식을 사용합니다.
        return logs.stream()
                .map(iLog -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
                    return ILogResponse.fromEntity(iLog, bestComment, objectMapper, user.getUserProfile().getUserId());
                })
                .collect(Collectors.toList());
    }

    // 모바일에서 사용 중입니다. //
    // 이전 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getPreviousLog(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findFirstByUserProfileUserIdAndLogDateLessThanOrderByLogDateDesc(user.getUserProfile().getUserId(), date)
                .map(log -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(log.getId()).orElse(null);
                    return ILogResponse.fromEntity(log, bestComment, objectMapper, user.getUserProfile().getUserId());
                })
                .orElse(null);
    }

    // 모바일에서 사용 중입니다. //
    // 다음 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getNextLog(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findFirstByUserProfileUserIdAndLogDateGreaterThanOrderByLogDateAsc(user.getUserProfile().getUserId(), date)
                .map(log -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(log.getId()).orElse(null);
                    return ILogResponse.fromEntity(log, bestComment, objectMapper, user.getUserProfile().getUserId());
                })
                .orElse(null);
    }

    // 🆕 [추가] 특정 사용자의 ID로 일기 목록 페이징 조회 (친구 마이페이지용)
    @Transactional(readOnly = true)
    public Page<ILogResponse> getPagedLogsByUserId(Long userId, User currentUser, Pageable pageable) {
        // 1. userId로 User를 찾습니다. User가 없다면 예외를 발생시킵니다.
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // ✅ [개선] N+1 문제 해결을 위해 DTO로 직접 조회하는 Repository 메서드를 호출합니다.
        Page<ILogResponse> logsPageDto;
        // ✅ [수정] 조회 대상 ID와 현재 사용자 ID를 비교
        if (userId.equals(currentUser.getId())) {
            // 2-1. ID가 같으면 '내' 마이페이지이므로 모든 일기를 조회합니다.
            logsPageDto = ilogRepository.findAllAsDtoByUserProfileUserId(
                    targetUser.getUserProfile().getUserId(),
                    currentUser.getUserProfile().getUserId(),
                    pageable);
        } else {
            // 2-2. ID가 다르면 '다른 사람' 마이페이지이므로 '공개'된 일기만 조회합니다.
            logsPageDto = ilogRepository.findAsDtoByUserProfileUserIdAndVisibility(
                    targetUser.getUserProfile().getUserId(),
                    ILog.Visibility.PUBLIC,
                    currentUser.getUserProfile().getUserId(),
                    pageable);
        }

        // 3. 조회된 DTO 페이지를 그대로 반환합니다.
        return logsPageDto;
    }

    // 🆕 [추가] 특정 사용자가 '좋아요' 누른 일기 목록 조회
    @Transactional(readOnly = true)
    public Page<ILogFeedResponseDto> getLikedILogsByUser(Long targetUserId, User currentUser, String sortBy, int page, int size) {
        // 1. 정렬 기준(sortBy)에 따라 Sort 객체를 생성합니다.
        Sort sort;
        switch (sortBy) {
            case "uploaded_at":
                // 일기 작성 최신순
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "popular":
                // 인기순 (좋아요 많은 순)
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case "liked_at":
            default:
                // 좋아요 누른 최신순 (기본값)
                // Likes 엔티티의 생성 시간(createdAt)을 기준으로 정렬해야 하므로, Repository 쿼리에서 직접 처리합니다.
                // 여기서는 정렬 객체를 비워두거나, Repository에서 사용할 특별한 값을 전달할 수 있습니다.
                // 이 예제에서는 sortBy 문자열을 그대로 Repository에 전달하여 처리하도록 하겠습니다.
                sort = Sort.unsorted(); // Repository에서 직접 처리할 것이므로 여기서는 정렬 없음을 명시
                break;
        }

        // 2. Pageable 객체를 생성합니다.
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. ✅ [개선] N+1 문제 해결을 위해 DTO로 직접 조회하는 Repository 메서드를 호출합니다.
        if ("liked_at".equals(sortBy)) {
            // '좋아요 누른 순'은 별도의 쿼리로 처리
            return ilogRepository.findLikedILogsAsDtoByUserOrderByLikedAt(
                    targetUserId,
                    currentUser.getUserProfile().getUserId(),
                    pageable);
        } else {
            // '인기순', '작성순'은 Pageable에 설정된 Sort를 이용
            return ilogRepository.findLikedILogsAsDtoByUser(
                    targetUserId,
                    currentUser.getUserProfile().getUserId(),
                    pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<ILogFeedResponseDto> getFeedForUser(User currentUser, int page, int size) {
        // ✅ [수정] pageable 객체를 먼저 생성해야 if문에서 사용할 수 있습니다.
        // 1. 최신순(createdAt 기준 내림차순)으로 정렬 조건을 설정한다.
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. 내가 팔로우하는 사람들의 프로필 ID 목록을 조회한다.
        List<Long> followingProfileIds = friendRepository.findAllByFollower(currentUser)
                .stream()
                .map(friend -> friend.getFollowing().getUserProfile().getUserId())
                .collect(Collectors.toList());

        // 3. 나를 팔로우하는 사람들의 프로필 ID 목록을 조회한다.
        List<Long> followerProfileIds = friendRepository.findAllByFollowing(currentUser)
                .stream()
                .map(friend -> friend.getFollower().getUserProfile().getUserId())
                .collect(Collectors.toList());

        // 4. '서로 팔로우'하는 친구(friends)의 프로필 ID 목록을 계산한다. (교집합)
        List<Long> friendProfileIds = followingProfileIds.stream()
                .filter(followerProfileIds::contains)
                .collect(Collectors.toList());

        // 5. ✅ [수정] Repository의 변경된 메서드(findCustomFeedForUser)를 호출합니다.
        // N+1 문제를 방지하고 '친구 공개' 게시물까지 포함하여 피드를 조회합니다.
        return ilogRepository.findCustomFeedForUser(
                currentUser.getUserProfile().getUserId(),
                followingProfileIds,
                friendProfileIds,
                ILog.Visibility.PUBLIC,       // '전체 공개' 상태값 전달
                ILog.Visibility.FRIENDS_ONLY, // '친구 공개' 상태값 전달
                pageable
        );
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

        // 5. 친구 포스트 알림 생성 로직 추가
        // 비공개(PRIVATE) 일기가 아닐 경우에만 알림을 보냅니다.
        if (savedIlog.getVisibility() != ILog.Visibility.PRIVATE) {
            // 일기 작성자를 팔로우하는 모든 사용자(친구)를 조회합니다.
            List<Friend> followers = friendRepository.findAllByFollowing(user);

            // 각 팔로워에게 알림을 보냅니다.
            for (Friend friend : followers) {
                User followerUser = friend.getFollower();
                // 작성자 본인에게는 알림을 보내지 않습니다.
                if (!followerUser.getId().equals(user.getId())) {
                    notificationComposer.friendDiaryCreated(
                            followerUser.getId(), // 알림 수신자 ID
                            user.getId(),         // 일기 작성자 ID
                            savedIlog.getId(),    // 일기 ID
                            user.getUserProfile().getNickname(), // 일기 작성자 이름 (UserProfile에서 가져옴)
                            savedIlog.getLogDate().toString() // 일기 작성 날짜 (ISO 형식)
                    );
                }
            }
        }

        // 6. 저장된 Entity를 Response DTO로 변환하여 반환
        // 새로 생성된 일기에는 댓글이 없으므로 bestComment는 null 입니다.
        return ILogResponse.fromEntity(savedIlog, null, objectMapper, user.getUserProfile().getUserId());
    }

    // 특정 날짜 일기 조회
    @Transactional(readOnly = true)
    public ILogResponse getLogByDate(User user, LocalDate date) {
        // ✅ [개선] Optional과 map을 사용하여 코드를 더 간결하고 Null-safe하게 만듭니다.
        return ilogRepository.findByUserProfileUserIdAndLogDate(user.getUserProfile().getUserId(), date)
                // ✅ [개선] 베스트 댓글 조회 로직 제거
                .map(log -> ILogResponse.fromEntity(log, null, objectMapper, user.getUserProfile().getUserId()))
                .orElse(null);
    }

    // 일기 삭제
    @Transactional
    public void deleteLog(User user, Long logId) {
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("ILog not found with id: " + logId));

        if (!log.getUserProfile().getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to delete this log.");
        }

        // 1. 댓글 및 댓글의 좋아요 삭제
        List<IlogComment> comments = ilogCommentRepository.findAllByIlog(log);
        if (comments != null && !comments.isEmpty()) {
            ilogCommentLikeRepository.deleteAllByIlogCommentIn(comments);
            ilogCommentRepository.deleteAllByIlog(log);
        }

        // 2. 일기 자체의 좋아요 삭제
        likesRepository.deleteAllByiLog(log);

        // 3. 이미지 삭제
        if (log.getImgUrl() != null && !log.getImgUrl().isBlank()) {
            try {
                List<String> imageUrls = objectMapper.readValue(
                        log.getImgUrl(),
                        new TypeReference<List<String>>() {
                        }
                );

                for (String url : imageUrls) {
                    firebaseService.deleteFile(url);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete images. Halting log deletion.", e);
            }
        }

        // 4. 모든 종속성 삭제 후 일기 삭제
        ilogRepository.delete(log);
    }


    // 일기 수정
    @Transactional
    public ILogResponse updateLog(Long logId, User user, ILogUpdateRequest request, List<MultipartFile> newImages) throws IOException {
        // 1. 일기 조회 및 수정 권한 확인
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("ILog not found with id: " + logId));

        // [수정] @MapsId 관계로 인해 userProfile.getUserId()가 null일 수 있으므로,
        // userProfile에 연결된 User 객체의 ID를 통해 비교해야 정확합니다.
        if (!log.getUserProfile().getUser().getId().equals(user.getId())) {
            // ✅ [개선] 권한 없음 예외는 SecurityException을 사용하는 것이 더 의미에 맞습니다.
            throw new SecurityException("You do not have permission to update this log.");
        }

        // 2. 이미지 변경 처리
        // 2-1. DB에 저장된 기존 이미지 URL 목록을 가져옵니다.
        List<String> oldImageUrls = new ArrayList<>();
        if (log.getImgUrl() != null && !log.getImgUrl().isBlank()) {
            oldImageUrls = objectMapper.readValue(log.getImgUrl(), new TypeReference<>() {
            });
        }

        // 2-2. 프론트에서 보낸 '유지할 이미지' 목록에 없는 기존 이미지는 Firebase에서 삭제합니다.
        // 2-2. 프론트에서 보낸 '유지할 이미지' 목록에 없는 기존 이미지는 Firebase에서 삭제합니다.
        List<String> existingUrlsToKeep = request.getExistingImageUrls() != null ? request.getExistingImageUrls() : new ArrayList<>();

        List<String> urlsToDelete = oldImageUrls.stream()
                .filter(oldUrl -> !existingUrlsToKeep.contains(oldUrl))
                .collect(Collectors.toList());

        for (String url : urlsToDelete) {
            try {
                firebaseService.deleteFile(url);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete existing images. Halting log update.", e);
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
        // ✅ [개선] 베스트 댓글 조회 로직 제거
        return ILogResponse.fromEntity(log, null, objectMapper, user.getUserProfile().getUserId());
    }

    /**
     * ✅ [신규] 특정 일기의 공유 ID를 조회하거나 생성합니다.
     * @param logId 공유 ID를 생성할 일기의 ID
     * @return 생성되거나 조회된 공유 ID
     */
    @Transactional
    public String getOrCreateShareId(Long logId) {
        // 1. 일기를 조회합니다. 없으면 예외를 발생시킵니다.
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("ILog not found with id: " + logId));

        // 2. shareId가 이미 존재하는지 확인합니다.
        if (log.getShareId() != null && !log.getShareId().isBlank()) {
            return log.getShareId(); // 이미 있으면 그대로 반환
        }

        // 3. shareId가 없으면, 중복되지 않는 9자리 랜덤 숫자를 생성합니다.
        String newShareId;
        do {
            // 100,000,000 ~ 999,999,999 사이의 9자리 숫자 생성
            int randomNumber = 100_000_000 + ThreadLocalRandom.current().nextInt(900_000_000);
            newShareId = String.valueOf(randomNumber);
        } while (ilogRepository.existsByShareId(newShareId)); // DB에 이미 존재하는 ID인지 확인

        // 4. 생성된 고유 ID를 엔티티에 설정하고 저장합니다.
        // @Transactional에 의해 메서드 종료 시 자동으로 UPDATE 쿼리가 실행됩니다.
        log.setShareId(newShareId);

        return newShareId;
    }

    /**
     * ✅ [신규] 공유 ID로 일기를 조회합니다. (권한 검사 포함)
     * @param shareId 조회할 일기의 공유 ID
     * @return 조회된 일기 데이터
     */
    @Transactional(readOnly = true)
    public ILogFeedResponseDto getSharedLog(String shareId) {
        // 1. shareId로 일기를 찾습니다. 없으면 예외를 발생시킵니다.
        ILog log = ilogRepository.findByShareId(shareId)
                .orElseThrow(() -> new IllegalArgumentException("공유된 일기를 찾을 수 없습니다."));

        // 2. 현재 로그인한 사용자 정보를 가져옵니다. (로그인 안했으면 null)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            // ✅ [수정] Principal에서 User 객체를 직접 가져와 안전하게 ID를 추출합니다.
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                currentUserId = ((User) principal).getId();
            }
        }

        // 3. 일기의 공개 범위를 확인하고 접근 권한을 검사합니다.
        switch (log.getVisibility()) {
            case PRIVATE:
                // 비공개 일기는 절대 공유할 수 없습니다.
                throw new IllegalStateException("비공개 처리된 일기입니다.");

            case FRIENDS_ONLY:
                // 친구 공개 일기는 로그인 상태여야 하고, '서로 친구' 관계여야 합니다.
                if (currentUserId == null) {
                    throw new IllegalStateException("친구에게만 공개된 일기입니다. 로그인이 필요합니다.");
                }

                Long authorId = log.getUserProfile().getUser().getId();

                // 작성자 본인이면 통과
                if (authorId.equals(currentUserId)) break;

                // '서로 친구'인지 확인 (A->B, B->A 모두 팔로우)
                boolean isFollowing = friendRepository.existsByFollowerIdAndFollowingId(currentUserId, authorId);
                boolean isFollowedBy = friendRepository.existsByFollowerIdAndFollowingId(authorId, currentUserId);

                if (!isFollowing || !isFollowedBy) {
                    throw new IllegalStateException("친구에게만 공개된 일기입니다.");
                }
                break;
        }

        // 4. 모든 검사를 통과하면, 피드 형식으로 변환하여 반환합니다.
        return ILogFeedResponseDto.fromEntity(log, currentUserId);
    }
}
