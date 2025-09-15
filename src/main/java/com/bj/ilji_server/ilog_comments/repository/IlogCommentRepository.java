package com.bj.ilji_server.ilog_comments.repository;

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
    // ✅ [개선] like_count 컬럼을 직접 사용하여 정렬하도록 쿼리 변경 (성능 향상)
    @Query("SELECT DISTINCT c FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<IlogComment> findTopLevelCommentsByIlogIdOrderByLikes(@Param("ilogId") Long ilogId);

    /**
     * 특정 일기(ilogId)에 달린 최상위 댓글 목록을 '최신순'으로 조회합니다.
     * @param ilogId 일기 ID
     * @return 정렬된 댓글 목록
     */
    @Query("SELECT DISTINCT c FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "ORDER BY c.createdAt DESC")
    List<IlogComment> findTopLevelCommentsByIlogIdOrderByRecent(@Param("ilogId") Long ilogId);

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