package com.bj.ilji_server.ilog_comment_likes.repository;

import com.bj.ilji_server.ilog_comment_likes.entity.IlogCommentLike;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IlogCommentLikeRepository extends JpaRepository<IlogCommentLike, Long> {

    /**
     * 사용자 프로필과 댓글 정보를 기준으로 '좋아요' 존재 여부를 확인합니다.
     * @param userProfile 사용자 프로필 엔티티
     * @param ilogComment 댓글 엔티티
     * @return Optional<IlogCommentLike>
     */
    @Query("SELECT icl FROM IlogCommentLike icl WHERE icl.userProfile = :userProfile AND icl.ilogComment = :ilogComment")
    Optional<IlogCommentLike> findByUserProfileAndIlogComment(@Param("userProfile") UserProfile userProfile, @Param("ilogComment") IlogComment ilogComment);

    /**
     * 특정 댓글(commentId)에 대한 모든 '좋아요' 목록을 조회합니다.
     * N+1 문제 방지를 위해 @EntityGraph를 사용하여 userProfile을 함께 조회(fetch)합니다.
     * @param commentId 댓글 ID
     * @return List<IlogCommentLike>
     */
    @EntityGraph(attributePaths = {"userProfile"})
    List<IlogCommentLike> findAllByIlogComment_Id(Long commentId);

    boolean existsByIlogComment_IdAndUserProfile_UserId(Long commentId, Long userId);
}