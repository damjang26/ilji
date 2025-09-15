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
    // ✅ [개선] FETCH JOIN으로 N+1 문제를 방지하고, 논리적 삭제(isDeleted)된 댓글을 필터링합니다.
    // 1. SELECT DISTINCT c: Fetch Join으로 발생할 수 있는 중복된 부모 댓글(c)을 제거합니다.
    // 2. LEFT JOIN FETCH c.user: 댓글 작성자 정보를 함께 조회합니다.
    // 3. LEFT JOIN IlogCommentLike l: 좋아요 수를 계산하기 위해 조인합니다.
    // 4. WHERE 조건:
    //    - c.ilog.id = :ilogId AND c.parent IS NULL: 특정 일기의 최상위 댓글만 대상으로 합니다.
    //    - (c.isDeleted = false OR ...): 삭제되지 않았거나, 삭제되었더라도 활성 자식 댓글이 있는 경우 스레드 유지를 위해 포함합니다.
    // 5. GROUP BY: 좋아요 수를 세기 위해 그룹화합니다.
    // 6. ORDER BY: 좋아요 수, 최신순으로 정렬합니다.
    @Query("SELECT DISTINCT c FROM IlogComment c " +
           "LEFT JOIN FETCH c.userProfile " +
           "LEFT JOIN IlogCommentLike l ON l.ilogComment = c " +
           "WHERE c.ilog.id = :ilogId AND c.parent IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (SELECT 1 FROM c.children ch WHERE ch.isDeleted = false)) " +
           "GROUP BY c, c.id, c.userProfile.userId, c.createdAt " + // GROUP BY 절에 SELECT 및 ORDER BY에 사용된 모든 non-aggregate 컬럼 포함
           "ORDER BY COUNT(l) DESC, c.createdAt DESC")
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
}