package com.bj.ilji_server.ilog_comments.service;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.ilog_comments.dto.IlogCommentCreateRequest;
import com.bj.ilji_server.ilog_comments.dto.IlogCommentDto;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.ilog_comments.repository.IlogCommentRepository;
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

    /**
     * 특정 일기의 모든 댓글을 계층 구조(댓글 + 대댓글)로 조회합니다.
     * @param ilogId 일기 ID
     * @return 계층적으로 구성된 댓글 DTO 목록
     */
    public List<IlogCommentDto> getCommentsByIlogId(Long ilogId, String sortBy) {
        List<IlogComment> topLevelComments;

        // 1. sortBy 값에 따라 적절한 Repository 메서드를 호출합니다.
        if ("recent".equalsIgnoreCase(sortBy)) {
            // '최신순'으로 정렬
            topLevelComments = ilogCommentRepository.findTopLevelCommentsByIlogIdOrderByRecent(ilogId);
        } else {
            // 기본값인 '좋아요순'으로 정렬
            topLevelComments = ilogCommentRepository.findTopLevelCommentsByIlogIdOrderByLikes(ilogId);
        }

        // 2. 조회된 엔티티 목록을 DTO 목록으로 변환합니다.
        //    IlogCommentDto.from() 메서드가 재귀적으로 대댓글까지 처리해줍니다.
        return topLevelComments.stream()
                .map(IlogCommentDto::from)
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
    public IlogCommentDto createComment(Long ilogId, IlogCommentCreateRequest request, User currentUser) {
        // 1. 댓글을 추가할 일기(ILog)를 조회합니다. 없으면 예외를 발생시킵니다.
        ILog iLog = iLogRepository.findById(ilogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기를 찾을 수 없습니다. id=" + ilogId));

        // 2. 대댓글인 경우, 부모 댓글을 조회합니다.
        IlogComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = ilogCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다. id=" + request.getParentCommentId()));
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

        // 5. 저장된 엔티티를 DTO로 변환하여 반환합니다.
        return IlogCommentDto.from(savedComment);
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
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. id=" + commentId));

        // 2. 댓글 작성자의 ID와 현재 로그인한 사용자의 ID를 비교하여 소유권을 확인합니다.
        if (!comment.getUserProfile().getUserId().equals(currentUser.getId())) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        // 3. 활성화된(삭제되지 않은) 자식 댓글이 있는지 확인합니다.
        boolean hasActiveChildren = comment.getChildren().stream()
                .anyMatch(child -> !child.isDeleted());

        if (hasActiveChildren) {
            // 4. 자식 댓글이 있으면 논리적 삭제(soft delete)를 수행합니다.
            comment.softDelete();
        } else {
            // 5. 자식 댓글이 없으면 물리적 삭제(hard delete)를 수행합니다.
            //    만약 이 댓글의 부모가 논리적으로 삭제된 상태이고, 이 댓글이 마지막 자식이었다면,
            //    부모 댓글도 함께 삭제하는 로직을 추가할 수 있습니다. (선택적 심화 과정)
            ilogCommentRepository.delete(comment);
        }
    }
}