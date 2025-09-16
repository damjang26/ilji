package com.bj.ilji_server.user_profile.repository;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("SELECT up FROM UserProfile up JOIN up.user u " +
           "WHERE u.id != :currentUserId " + 
           "AND (LOWER(u.email) LIKE LOWER('%' || :query || '%') OR LOWER(up.nickname) LIKE LOWER('%' || :query || '%'))")
    List<UserProfile> searchByEmailOrNickname(@Param("query") String query, @Param("currentUserId") Long currentUserId);

    // 닉네임으로 프로필이 존재하는지 확인하는 메서드 추가
    boolean existsByNickname(String nickname);
}
