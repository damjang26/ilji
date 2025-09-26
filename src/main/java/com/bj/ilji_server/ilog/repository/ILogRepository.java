package com.bj.ilji_server.ilog.repository;

import com.bj.ilji_server.ilog.dto.ILogFeedResponseDto;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.entity.ILog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ILogRepository extends JpaRepository<ILog, Long>  {

    /**
     * 특정 사용자의 일기 중, 주어진 기간에 해당하는 모든 일기를 조회합니다.
     * JOIN FETCH를 사용하여 연관된 UserProfile을 함께 조회하여 N+1 문제를 방지합니다.
     *
     * @param userProfileId 조회할 사용자의 프로필 ID
     * @param startDate     조회 시작일
     * @param endDate       조회 종료일
     * @return 기간 내의 ILog 리스트
     */
    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile " +
            "WHERE i.userProfile.userId = :userProfileId " +
            "AND i.logDate BETWEEN :startDate AND :endDate " +
            "ORDER BY i.logDate ASC")
    List<ILog> findByUserProfileUserIdAndLogDateBetweenWithUserProfile(
            @Param("userProfileId") Long userProfileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ [개선] N+1 문제 해결을 위해 JOIN FETCH를 사용하여 ILog와 UserProfile을 한 번의 쿼리로 조회합니다.
//    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId ORDER BY i.logDate ASC")
//    List<ILog> findAllByUserProfileUserIdWithUserProfile(@Param("userProfileId") Long userProfileId);

    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId AND i.logDate = :logDate")
    Optional<ILog> findByUserProfileUserIdAndLogDate(@Param("userProfileId") Long userProfileId, @Param("logDate") LocalDate logDate);


    // ---------------------------------------------------
    // 🆕 마이페이지용 일기 목록 조회 (N+1 문제 해결)
    // ---------------------------------------------------
    @Query(value = "SELECT new com.bj.ilji_server.ilog.dto.ILogResponse(" +
            "   i, " +
            "   null, " + // 베스트 댓글 조회 로직 제거
            "   EXISTS (SELECT 1 FROM Likes l WHERE l.iLog = i AND l.userProfile.userId = :currentUserId)" +
            ") " +
            "FROM ILog i JOIN FETCH i.userProfile " +
            "WHERE i.userProfile.userId = :targetUserId AND i.visibility = :visibility",
            countQuery = "SELECT count(i) FROM ILog i WHERE i.userProfile.userId = :targetUserId AND i.visibility = :visibility")
    Page<ILogResponse> findAsDtoByUserProfileUserIdAndVisibility(
            @Param("targetUserId") Long targetUserId,
            @Param("visibility") ILog.Visibility visibility,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );

    @Query(value = "SELECT new com.bj.ilji_server.ilog.dto.ILogResponse(" +
            "   i, " +
            "   null, " + // 베스트 댓글 조회 로직 제거
            "   EXISTS (SELECT 1 FROM Likes l WHERE l.iLog = i AND l.userProfile.userId = :currentUserId)" +
            ") " +
            "FROM ILog i JOIN FETCH i.userProfile " +
            "WHERE i.userProfile.userId = :targetUserId",
            countQuery = "SELECT count(i) FROM ILog i WHERE i.userProfile.userId = :targetUserId")
    Page<ILogResponse> findAllAsDtoByUserProfileUserId(
            @Param("targetUserId") Long targetUserId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );

    // ---------------------------------------------------
    // 🆕 PostList 일기 목록 조회
    // ---------------------------------------------------
    // ✅ [개선] N+1 문제 해결을 위해 DTO로 직접 조회하도록 변경
    @Query(value = "SELECT new com.bj.ilji_server.ilog.dto.ILogFeedResponseDto(" +
            "   i, " +
            "   null, " + // 베스트 댓글 조회 로직 제거
            "   EXISTS (SELECT 1 FROM Likes l WHERE l.iLog = i AND l.userProfile.userId = :currentUserProfileId)" +
            ") " +
            "FROM ILog i JOIN FETCH i.userProfile " +
            "WHERE i.userProfile.userId = :currentUserProfileId " + // 1. 내 글
            "OR (i.userProfile.userId IN :followingProfileIds AND i.visibility = :publicVisibility) " + // 2. 내가 팔로우하는 사람의 전체 공개 글
            "OR (i.userProfile.userId IN :friendProfileIds AND i.visibility = :friendsVisibility)", // 3. 서로 팔로우하는 사람의 친구 공개 글
            countQuery = "SELECT count(i) FROM ILog i " +
                    "WHERE i.userProfile.userId = :currentUserProfileId OR " +
                    "(i.userProfile.userId IN :followingProfileIds AND i.visibility = :publicVisibility) " +
                    "OR (i.userProfile.userId IN :friendProfileIds AND i.visibility = :friendsVisibility)")
    Page<ILogFeedResponseDto> findCustomFeedForUser(
            @Param("currentUserProfileId") Long currentUserProfileId,
            @Param("followingProfileIds") List<Long> followingProfileIds,
            @Param("friendProfileIds") List<Long> friendProfileIds,
            @Param("publicVisibility") ILog.Visibility publicVisibility,
            @Param("friendsVisibility") ILog.Visibility friendsVisibility,
            Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자가 '좋아요' 누른 일기 목록 조회 (N+1 문제 해결)
    // ---------------------------------------------------
    @Query(value = "SELECT new com.bj.ilji_server.ilog.dto.ILogFeedResponseDto(" +
            "   i, " +
            "   null, " + // 베스트 댓글 조회 로직 제거
            "   EXISTS (SELECT 1 FROM Likes l_check WHERE l_check.iLog = i AND l_check.userProfile.userId = :currentUserId)" +
            ") " +
            "FROM ILog i JOIN i.likes l " +
            "WHERE l.userProfile.userId = :targetUserId",
            countQuery = "SELECT count(i) FROM ILog i JOIN i.likes l " +
                    "WHERE l.userProfile.userId = :targetUserId")
    Page<ILogFeedResponseDto> findLikedILogsAsDtoByUser(
            @Param("targetUserId") Long targetUserId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자가 '좋아요' 누른 일기 목록 조회 ('좋아요 누른 순' 정렬, N+1 문제 해결)
    // ---------------------------------------------------
    @Query(value = "SELECT new com.bj.ilji_server.ilog.dto.ILogFeedResponseDto(" +
            "   i, " +
            "   null, " + // 베스트 댓글 조회 로직 제거
            "   EXISTS (SELECT 1 FROM Likes l_check WHERE l_check.iLog = i AND l_check.userProfile.userId = :currentUserId)" +
            ") " +
            "FROM ILog i JOIN i.likes l " +
            "WHERE l.userProfile.userId = :targetUserId ORDER BY l.createdAt DESC",
            countQuery = "SELECT count(i) FROM ILog i JOIN i.likes l " +
                    "WHERE l.userProfile.userId = :targetUserId")
    Page<ILogFeedResponseDto> findLikedILogsAsDtoByUserOrderByLikedAt(
            @Param("targetUserId") Long targetUserId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자의 총 게시물 수 조회
    // ---------------------------------------------------
    long countByUserProfileUserId(Long userProfileId);

    boolean existsByShareId(String newShareId);

    // ✅ [신규] shareId로 ILog 엔티티를 찾는 메서드
    Optional<ILog> findByShareId(String shareId);
}
