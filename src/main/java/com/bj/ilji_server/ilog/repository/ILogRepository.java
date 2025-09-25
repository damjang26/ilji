package com.bj.ilji_server.ilog.repository;

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

    // 모바일에서 사용 중입니다. //
    // ✅ [개선] N+1 문제 해결을 위해 JOIN FETCH를 사용하여 ILog와 UserProfile을 한 번의 쿼리로 조회합니다.
    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId ORDER BY i.logDate ASC")
    List<ILog> findAllByUserProfileUserIdWithUserProfile(@Param("userProfileId") Long userProfileId);

    // 모바일에서 사용 중입니다. //
    // 이전 일기 (현재 날짜보다 작은 것 중 제일 최근 것 하나)
    Optional<ILog> findFirstByUserProfileUserIdAndLogDateLessThanOrderByLogDateDesc(Long userProfileUserId, LocalDate logDate);

    // 모바일에서 사용 중입니다. //
    // 다음 일기 (현재 날짜보다 큰 것 중 제일 가까운 것 하나)
    Optional<ILog> findFirstByUserProfileUserIdAndLogDateGreaterThanOrderByLogDateAsc(Long userProfileUserId, LocalDate logDate);


    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId AND i.logDate = :logDate")
    Optional<ILog> findByUserProfileUserIdAndLogDate(@Param("userProfileId") Long userProfileId, @Param("logDate") LocalDate logDate);

    // 🆕 [추가] 특정 사용자의 특정 공개 상태인 일기 목록 조회 (친구 마이페이지용)
    // N+1 문제 방지를 위해 JOIN FETCH 사용
    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId AND i.visibility = :visibility ORDER BY i.logDate ASC")
    List<ILog> findByProfileAndVisibility(@Param("userProfileId") Long userProfileId, @Param("visibility") ILog.Visibility visibility);

    // 🆕 [추가] 특정 사용자의 특정 공개 상태인 일기 목록 페이징 조회
    @Query(value = "SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId AND i.visibility = :visibility",
           countQuery = "SELECT count(i) FROM ILog i WHERE i.userProfile.userId = :userProfileId AND i.visibility = :visibility")
    Page<ILog> findByUserProfileUserIdAndVisibility(@Param("userProfileId") Long userProfileId, @Param("visibility") ILog.Visibility visibility, Pageable pageable);

    // 🆕 [추가] 특정 사용자의 모든 일기 목록 페이징 조회 (내 마이페이지용)
    @Query(value = "SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId",
           countQuery = "SELECT count(i) FROM ILog i WHERE i.userProfile.userId = :userProfileId")
    Page<ILog> findAllByUserProfileUserId(@Param("userProfileId") Long userProfileId, Pageable pageable);


    @Query(value = "SELECT i FROM ILog i JOIN FETCH i.userProfile " +
                   "WHERE i.userProfile.userId = :currentUserProfileId OR " +
                   "(i.userProfile.userId IN :followingProfileIds AND i.visibility = :publicVisibility)",
           countQuery = "SELECT count(i) FROM ILog i " +
                        "WHERE i.userProfile.userId = :currentUserProfileId OR " +
                        "(i.userProfile.userId IN :followingProfileIds AND i.visibility = :publicVisibility)")
    Page<ILog> findFeedByUserProfileIdAndFollowingIds(
            @Param("currentUserProfileId") Long currentUserProfileId,
            @Param("followingProfileIds") List<Long> followingProfileIds,
            @Param("publicVisibility") ILog.Visibility publicVisibility,
            Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자가 '좋아요' 누른 일기 목록 조회
    // ---------------------------------------------------
    @Query(value = "SELECT i FROM ILog i JOIN i.likes l " +
                   "WHERE l.userProfile.userId = :userId",
           countQuery = "SELECT count(i) FROM ILog i JOIN i.likes l " +
                        "WHERE l.userProfile.userId = :userId")
    Page<ILog> findLikedILogsByUser(@Param("userId") Long userId, Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자가 '좋아요' 누른 일기 목록 조회 ('좋아요 누른 순' 정렬)
    // ---------------------------------------------------
    @Query(value = "SELECT i FROM ILog i JOIN i.likes l " +
                   "WHERE l.userProfile.userId = :userId ORDER BY l.createdAt DESC",
           countQuery = "SELECT count(i) FROM ILog i JOIN i.likes l " +
                        "WHERE l.userProfile.userId = :userId")
    Page<ILog> findLikedILogsByUserOrderByLikedAt(@Param("userId") Long userId, Pageable pageable);

    // ---------------------------------------------------
    // 🆕 [추가] 특정 사용자의 총 게시물 수 조회
    // ---------------------------------------------------
    long countByUserProfileUserId(Long userProfileId);
}
