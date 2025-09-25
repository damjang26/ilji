package com.bj.ilji_server.ilog_comments.repository;

import com.bj.ilji_server.ilog_comments.dto.IlogCommentResponseDto;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IlogCommentRepository extends JpaRepository<IlogComment, Long> {

    /**
     * 특정 일기(ilogId)에 달린 최상위 댓글 목록을 조회합니다.
     * 정렬 순서:
     * 1. 좋아요(like) 개수가 많은 순 (내림차순)
     * 2. 좋아요 개수가 같으면, 가장 최근에 작성된 순 (내림차순)
     *
     * @param ilogId 일기 ID
     * @return 정렬된 댓글 목록
     */
    // ✅ [수정] 현재 사용자의 '좋아요' 여부(isLiked)를 함께 조회하도록 쿼리 변경
    // ✅ [개선] 반환 타입을 DTO로 직접 지정하여 타입 안정성 확보 및 서비스 로직 간소화
    // ✅ [수정] JPQL의 DTO 클래스 이름을 반환 타입과 일치시킵니다. (IlogCommentResponseDTO -> IlogCommentResponseDto)
    @Query("SELECT new com.bj.ilji_server.ilog_comments.dto.IlogCommentResponseDto(c, " +
           "EXISTS (SELECT 1 FROM IlogCommentLike icl WHERE icl.ilogComment = c AND icl.userProfile.userId = :currentUserId)" +
           ") " +
           "FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<IlogCommentResponseDto> findTopLevelCommentsAsDtoByIlogIdOrderByLikes(@Param("ilogId") Long ilogId, @Param("currentUserId") Long currentUserId);

    /**
     * 특정 일기(ilogId)에 달린 최상위 댓글 목록을 '최신순'으로 조회합니다.
     * @param ilogId 일기 ID
     * @return 정렬된 댓글 목록
     */
    // ✅ [수정] JPQL의 DTO 클래스 이름을 반환 타입과 일치시킵니다. (IlogCommentResponseDTO -> IlogCommentResponseDto)
    @Query("SELECT new com.bj.ilji_server.ilog_comments.dto.IlogCommentResponseDto(c, " +
           "EXISTS (SELECT 1 FROM IlogCommentLike icl WHERE icl.ilogComment = c AND icl.userProfile.userId = :currentUserId)" +
           ") " +
           "FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.createdAt DESC")
    List<IlogCommentResponseDto> findTopLevelCommentsAsDtoByIlogIdOrderByRecent(@Param("ilogId") Long ilogId, @Param("currentUserId") Long currentUserId);

    @Query("SELECT c FROM IlogComment c JOIN FETCH c.userProfile WHERE c.id = :commentId")
    Optional<IlogComment> findByIdWithUser(@Param("commentId") Long commentId);

    /**
     * 특정 일기에서 가장 좋아요가 많은 최상위 댓글 1개를 조회합니다. (베스트 댓글 미리보기용)
     * 삭제되지 않은 댓글만 대상으로 합니다.
     * @param ilogId 일기 ID
     * @return 가장 좋아요가 많은 댓글 Optional
     */
    Optional<IlogComment> findTopByIlogIdAndIsDeletedFalseAndParentIsNullOrderByLikeCountDescCreatedAtDesc(Long ilogId);
}