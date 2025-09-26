package com.bj.ilji_server.ilog_comments.service;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.ilog_comments.dto.IlogCommentCreateRequest;
import com.bj.ilji_server.ilog_comments.dto.IlogCommentResponseDto;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.ilog_comments.repository.IlogCommentRepository;
import com.bj.ilji_server.notification.packing.NotificationComposer; // Import NotificationComposer
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IlogCommentService {

    private final IlogCommentRepository ilogCommentRepository;
    private final ILogRepository iLogRepository;
    private final NotificationComposer notificationComposer;

    /**
     * 특정 일기의 모든 댓글을 계층 구조(댓글 + 대댓글)로 조회합니다.
     * @param ilogId 일기 ID
     * @param currentUser 현재 로그인한 사용자 (null일 수 있음)
     * @return 계층적으로 구성된 댓글 DTO 목록
     */
    public List<IlogCommentResponseDto> getCommentsByIlogId(Long ilogId, String sortBy, User currentUser) {
        // 비로그인 사용자의 경우 ID를 -1L로 설정하여 유효하지 않은 사용자로 처리
        Long currentUserId = (currentUser != null) ? currentUser.getUserProfile().getUserId() : -1L;

        List<Object[]> results;
        // 1. sortBy 값에 따라 적절한 Repository 메서드를 호출합니다.
        if ("recent".equalsIgnoreCase(sortBy)) {
            // '최신순'으로 정렬
            results = ilogCommentRepository.findTopLevelCommentsWithLikeStatusOrderByRecent(ilogId, currentUserId);
        } else {
            // 기본값인 '좋아요순'으로 정렬
            results = ilogCommentRepository.findTopLevelCommentsWithLikeStatusOrderByLikes(ilogId, currentUserId);
        }

        // 2. ✅ [개선] 조회 결과를 DTO로 변환합니다.
        // 이 과정에서 답글(자식 댓글)의 '좋아요' 여부까지 정확하게 계산합니다.
        return results.stream()
                .map(result -> {
                    IlogComment comment = (IlogComment) result[0];
                    boolean isLiked = (boolean) result[1];
                    return IlogCommentResponseDto.from(comment, isLiked, currentUserId);
                })
                .collect(Collectors.toList());
    }

    /**
     * 새로운 댓글 또는 대댓글을 생성합니다.
     * @param ilogId 댓글을 달 일기 ID
     * @param request 댓글 내용과 부모 댓글 ID가 담긴 요청 DTO
     * @param currentUser 현재 로그인한 사용자
     * @return 생성된 댓글의 DTO
     */
    @Transactional
    public IlogCommentResponseDto createComment(Long ilogId, IlogCommentCreateRequest request, User currentUser) {
        // 1. 댓글을 추가할 일기(ILog)를 조회합니다. 없으면 예외를 발생시킵니다.
        ILog iLog = iLogRepository.findById(ilogId)
                .orElseThrow(() -> new IllegalArgumentException("ILog not found with id=" + ilogId));

        // 2. 대댓글인 경우, 부모 댓글을 조회합니다.
        IlogComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = ilogCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id=" + request.getParentCommentId()));
            // ✅ [추가] 대댓글(답글)에 또 다른 답글을 달 수 없도록 제한합니다.
            // 부모 댓글이 될 댓글이 이미 다른 댓글의 자식 댓글이라면 예외를 발생시킵니다.
            if (parentComment.getParent() != null) {
                throw new IllegalArgumentException("Replies can only be added to top-level comments.");
            }
       
        }

        // 3. 새로운 댓글 엔티티를 생성합니다.
        IlogComment newComment = IlogComment.builder()
                .ilog(iLog)
                .userProfile(currentUser.getUserProfile()) // 이 부분은 User 엔티티에 getUserProfile()이 있다는 가정하에 그대로 둡니다.
                .content(request.getContent())
                .parent(parentComment) // 부모 댓글 설정 (null일 수도 있음)
                .build();

        // 4. 생성된 댓글을 DB에 저장합니다.
        IlogComment savedComment = ilogCommentRepository.save(newComment);

        // 5. 일기의 댓글 수를 1 증가시킵니다.
        iLog.increaseCommentCount();

        // 6. 알림 생성
        // ✅ [개선] 댓글 종류에 따라 알림 수신자를 다르게 설정합니다.
        if (parentComment != null) {
            // 대댓글(답글)인 경우, 부모 댓글 작성자에게 알림을 보냅니다.
            Long parentCommentAuthorId = parentComment.getUserProfile().getUserId();
            // 자기 자신의 댓글에 답글을 다는 경우는 알림을 보내지 않습니다.
            if (!parentCommentAuthorId.equals(currentUser.getUserProfile().getUserId())) {
                notificationComposer.ilogCommentCreated(
                        parentCommentAuthorId, // 수신자: 부모 댓글 작성자
                        iLog.getId(), iLog.getLogDate(),
                        currentUser.getUserProfile().getUserId(), currentUser.getUserProfile().getNickname());
            }
        } else {
            // 최상위 댓글인 경우, 일기 작성자에게 알림을 보냅니다.
            Long iLogAuthorId = iLog.getUserProfile().getUserId();
            // 자기 자신의 일기에 댓글을 다는 경우는 알림을 보내지 않습니다.
            if (!iLogAuthorId.equals(currentUser.getUserProfile().getUserId())) {
                notificationComposer.ilogCommentCreated(
                        iLogAuthorId, // 수신자: 일기 작성자
                        iLog.getId(), iLog.getLogDate(),
                        currentUser.getUserProfile().getUserId(), currentUser.getUserProfile().getNickname());
            }
        }

        // 7. 저장된 엔티티를 DTO로 변환하여 반환합니다.
        // ✅ [수정] from 메서드 시그니처에 맞게 currentUserId를 전달합니다.
        return IlogCommentResponseDto.from(savedComment, false, currentUser.getUserProfile().getUserId());
    }

    /**
     * 댓글을 삭제합니다.
     * @param commentId 삭제할 댓글 ID
     * @param currentUser 현재 로그인한 사용자
     */
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        // 1. 삭제할 댓글을 조회합니다. (N+1 방지를 위해 User 정보 함께 fetch)
        IlogComment comment = ilogCommentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id=" + commentId));

        // 2. 댓글 작성자의 ID와 현재 로그인한 사용자의 ID를 비교하여 소유권을 확인합니다.
        if (!comment.getUserProfile().getUserId().equals(currentUser.getUserProfile().getUserId())) {
            throw new SecurityException("You do not have permission to delete this comment.");
        }

        // 3. 활성화된(삭제되지 않은) 자식 댓글이 있는지 확인합니다.
        boolean hasActiveChildren = comment.getChildren().stream()
                .anyMatch(child -> !child.isDeleted());

        if (hasActiveChildren) {
            // 4. 자식 댓글이 있으면 논리적 삭제(soft delete)를 수행합니다.
            comment.softDelete();
        } else {
            // 댓글이 물리적으로 삭제되기 전에, 해당 일기의 댓글 수를 1 감소시킵니다.
            comment.getIlog().decreaseCommentCount();

            // 5. 자식 댓글이 없으면 물리적 삭제(hard delete)를 수행합니다.
            //    만약 이 댓글의 부모가 논리적으로 삭제된 상태이고, 이 댓글이 마지막 자식이었다면,
            //    부모 댓글도 함께 삭제하는 로직을 추가할 수 있습니다. (선택적 심화 과정)
            ilogCommentRepository.delete(comment);
        }
    }
}