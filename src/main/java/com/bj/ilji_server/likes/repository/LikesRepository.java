package com.bj.ilji_server.likes.repository;

import java.util.List;
import com.bj.ilji_server.likes.entity.Likes;
import com.bj.ilji_server.ilog.entity.ILog;
import org.springframework.data.jpa.repository.EntityGraph;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    // 사용자 프로필과 일기 정보를 기준으로 '좋아요' 존재 여부를 확인하는 메서드
    @Query("SELECT l FROM Likes l WHERE l.userProfile = :userProfile AND l.iLog = :iLog")
    Optional<Likes> findByUserProfileAndILog(@Param("userProfile") UserProfile userProfile, @Param("iLog") ILog iLog);

    // 특정 일기(ilogId)에 대한 모든 '좋아요' 목록을 조회하는 메서드
    @EntityGraph(attributePaths = {"userProfile"})
    List<Likes> findAllByiLog_Id(Long ilogId);
}