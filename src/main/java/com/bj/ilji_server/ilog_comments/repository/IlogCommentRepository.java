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
    // ✅ [수정] DTO를 직접 생성하는 대신, 댓글 엔티티와 '좋아요' 여부를 Object 배열로 조회합니다.
    // 서비스 레이어에서 답글의 '좋아요' 여부까지 처리하기 위함입니다.
    @Query("SELECT c, " +
           "EXISTS (SELECT 1 FROM IlogCommentLike icl WHERE icl.ilogComment = c AND icl.userProfile.userId = :currentUserId) " +
           "FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<Object[]> findTopLevelCommentsWithLikeStatusOrderByLikes(@Param("ilogId") Long ilogId, @Param("currentUserId") Long currentUserId);

    /**
     * 특정 일기(ilogId)에 달린 최상위 댓글 목록을 '최신순'으로 조회합니다.
     * @param ilogId 일기 ID
     * @return 정렬된 댓글 목록
     */
    @Query("SELECT c, " +
           "EXISTS (SELECT 1 FROM IlogCommentLike icl WHERE icl.ilogComment = c AND icl.userProfile.userId = :currentUserId) " +
           "FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.createdAt DESC")
    List<Object[]> findTopLevelCommentsWithLikeStatusOrderByRecent(@Param("ilogId") Long ilogId, @Param("currentUserId") Long currentUserId);

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