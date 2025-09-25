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
    private final IlogCommentRepository ilogCommentRepository;
    private final FriendRepository friendRepository;
    private final FirebaseService firebaseService;
    private final ObjectMapper objectMapper;
    private final NotificationComposer notificationComposer; // NotificationComposer 주입

//     특정 사용자의 일기 목록 조회
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

    // 모바일에서 사용 중입니다. //
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

    // 🆕 [추가] 특정 사용자의 ID로 일기 목록 조회 (친구 마이페이지용)
    @Transactional(readOnly = true)
    // ✅ [수정] 'isLiked' 상태를 확인하기 위해 현재 사용자(currentUser) 정보를 함께 받습니다.
    public List<ILogResponse> getLogsByUserId(Long userId, User currentUser) {
        // 1. userId로 User를 찾습니다. User가 없다면 예외를 발생시킵니다.
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 2. 해당 사용자의 UserProfile ID와 '공개(PUBLIC)' 상태인 일기만 조회합니다.
        //    친구의 비공개 일기는 보여주면 안 되기 때문입니다.
        //    (findByProfileAndVisibility는 다음 단계에서 Repository에 추가할 예정입니다)
        List<ILog> logs = ilogRepository.findByProfileAndVisibility(targetUser.getUserProfile().getUserId(), ILog.Visibility.PUBLIC);

        // 3. 조회된 ILog 엔티티 목록을 ILogResponse DTO 목록으로 변환하여 반환합니다.
        // ✅ [수정] fromEntity 메서드의 시그니처에 맞게 베스트 댓글과 현재 사용자 ID를 전달합니다.
        return logs.stream()
                .map(iLog -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
                    // 'isLiked'는 현재 접속한 사용자를 기준으로 판단해야 하므로 currentUser의 ID를 넘깁니다.
                    return ILogResponse.fromEntity(iLog, bestComment, objectMapper, currentUser.getUserProfile().getUserId());
                })
                .collect(Collectors.toList());
    }

    // 🆕 [추가] 특정 사용자의 ID로 일기 목록 페이징 조회 (친구 마이페이지용)
    @Transactional(readOnly = true)
    public Page<ILogResponse> getPagedLogsByUserId(Long userId, User currentUser, Pageable pageable) {
        // 1. userId로 User를 찾습니다. User가 없다면 예외를 발생시킵니다.
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Page<ILog> logsPage;
        // ✅ [수정] 조회 대상 ID와 현재 사용자 ID를 비교
        if (userId.equals(currentUser.getId())) {
            // 2-1. ID가 같으면 '내' 마이페이지이므로 모든 일기를 조회합니다.
            logsPage = ilogRepository.findAllByUserProfileUserId(
                    targetUser.getUserProfile().getUserId(),
                    pageable);
        } else {
            // 2-2. ID가 다르면 '다른 사람' 마이페이지이므로 '공개'된 일기만 조회합니다.
            logsPage = ilogRepository.findByUserProfileUserIdAndVisibility(
                    targetUser.getUserProfile().getUserId(),
                    ILog.Visibility.PUBLIC,
                    pageable);
        }

        // 3. 조회된 Page<ILog>를 Page<ILogResponse>로 변환하여 반환합니다.
        return logsPage.map(iLog -> {
            IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
            return ILogResponse.fromEntity(iLog, bestComment, objectMapper, currentUser.getUserProfile().getUserId());
        });
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

        // 3. Repository를 호출하여 '좋아요' 누른 일기 목록을 조회합니다.
        //    (findLikedILogsByUser 메서드는 다음 단계에서 Repository에 추가해야 합니다.)
        Page<ILog> likedILogsPage;
        if ("liked_at".equals(sortBy)) {
            // '좋아요 누른 순'은 별도의 쿼리로 처리
            likedILogsPage = ilogRepository.findLikedILogsByUserOrderByLikedAt(targetUserId, pageable);
        } else {
            // '인기순', '작성순'은 Pageable에 설정된 Sort를 이용
            likedILogsPage = ilogRepository.findLikedILogsByUser(targetUserId, pageable);
        }

        // 4. 조회된 Page<ILog>를 Page<ILogFeedResponseDto>로 변환합니다.
        //    'isLiked' 여부는 현재 로그인한 사용자(currentUser)를 기준으로 판단합니다.
        return likedILogsPage.map(iLog -> {
            IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
            return ILogFeedResponseDto.fromEntity(iLog, bestComment, objectMapper, currentUser.getUserProfile().getUserId());
        });
    }

    @Transactional(readOnly = true)
    public ILogResponse getLogById(Long logId, User currentUser) {
        ILog log = ilogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기를 찾을 수 없습니다. id=" + logId));

        User author = log.getUserProfile().getUser();

        // 권한 확인
        boolean canView = false;
        if (author.getId().equals(currentUser.getId())) {
            // 1. 본인 글은 항상 볼 수 있음
            canView = true;
        } else {
            switch (log.getVisibility()) {
                case PUBLIC:
                    // 2. 전체 공개 글은 누구나 볼 수 있음
                    canView = true;
                    break;
                case FRIENDS_ONLY:
                    // 3. 친구 공개 글은 친구만 볼 수 있음 (요청자가 작성자를 팔로우하는 경우)
                    if (friendRepository.existsByFollowerAndFollowing(currentUser, author)) {
                        canView = true;
                    }
                    break;
                case PRIVATE:
                    // 4. 비공개 글은 본인 외 볼 수 없음 (위에서 이미 처리됨)
                    canView = false;
                    break;
            }
        }

        if (!canView) {
            throw new SecurityException("해당 일기를 볼 권한이 없습니다.");
        }

        IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(log.getId()).orElse(null);
        return ILogResponse.fromEntity(log, bestComment, objectMapper, currentUser.getUserProfile().getUserId());
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
        return feedPage.map(iLog -> {
            IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(iLog.getId()).orElse(null);
            return ILogFeedResponseDto.fromEntity(iLog, bestComment, objectMapper, currentUser.getUserProfile().getUserId());
        });
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
                .map(log -> {
                    IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(log.getId()).orElse(null);
                    return ILogResponse.fromEntity(log, bestComment, objectMapper, user.getUserProfile().getUserId());
                })
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
        IlogComment bestComment = ilogCommentRepository.findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(log.getId()).orElse(null);
        return ILogResponse.fromEntity(log, bestComment, objectMapper, user.getUserProfile().getUserId());
    }

}
