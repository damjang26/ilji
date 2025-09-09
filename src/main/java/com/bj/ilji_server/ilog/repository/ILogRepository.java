package com.bj.ilji_server.ilog.repository;

import com.bj.ilji_server.ilog.entity.ILog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ILogRepository extends JpaRepository<ILog, Long>  {

    // userId 기준으로 일기 목록 찾기 (날짜 오름차순 정렬)
    List<ILog> findByUserProfileUserIdOrderByLogDateAsc(Long userProfileUserId);

    // 특정 날짜 일기 찾기
    ILog findByUserProfileUserIdAndLogDate(Long userProfileUserId, LocalDate logDate);

    // 이전 일기 (현재 날짜보다 작은 것 중 제일 최근 것 하나)
    ILog findFirstByUserProfileUserIdAndLogDateLessThanOrderByLogDateDesc(Long userProfileUserId, LocalDate logDate);

    // 다음 일기 (현재 날짜보다 큰 것 중 제일 가까운 것 하나)
    ILog findFirstByUserProfileUserIdAndLogDateGreaterThanOrderByLogDateAsc(Long userProfileUserId, LocalDate logDate);

    // ✅ [수정] UserProfile의 ID 필드 이름이 'userId'이므로 쿼리에서도 동일하게 수정합니다.
    @Query("SELECT i FROM ILog i WHERE i.userProfile.userId = :currentUserProfileId OR (i.userProfile.userId IN :followingProfileIds AND i.visibility = :publicVisibility)")
    Page<ILog> findFeedByUserProfileIdAndFollowingIds(
            @Param("currentUserProfileId") Long currentUserProfileId,
            @Param("followingProfileIds") List<Long> followingProfileIds,
            @Param("publicVisibility") ILog.Visibility publicVisibility,
            Pageable pageable);
}
