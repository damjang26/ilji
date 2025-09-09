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

    // ✅ [개선] N+1 문제 해결을 위해 JOIN FETCH를 사용하여 ILog와 UserProfile을 한 번의 쿼리로 조회합니다.
    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId ORDER BY i.logDate ASC")
    List<ILog> findAllByUserProfileUserIdWithUserProfile(@Param("userProfileId") Long userProfileId);

    @Query("SELECT i FROM ILog i JOIN FETCH i.userProfile WHERE i.userProfile.userId = :userProfileId AND i.logDate = :logDate")
    Optional<ILog> findByUserProfileUserIdAndLogDate(@Param("userProfileId") Long userProfileId, @Param("logDate") LocalDate logDate);

    // 이전 일기 (현재 날짜보다 작은 것 중 제일 최근 것 하나)
    Optional<ILog> findFirstByUserProfileUserIdAndLogDateLessThanOrderByLogDateDesc(Long userProfileUserId, LocalDate logDate);

    // 다음 일기 (현재 날짜보다 큰 것 중 제일 가까운 것 하나)
    Optional<ILog> findFirstByUserProfileUserIdAndLogDateGreaterThanOrderByLogDateAsc(Long userProfileUserId, LocalDate logDate);

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
}
