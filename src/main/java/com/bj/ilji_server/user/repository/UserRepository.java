package com.bj.ilji_server.user.repository;

import com.bj.ilji_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Optional<User> findByEmail(String email);


    // 모든 사용자 PK만 가볍게 조회
    @Query("select u.id from User u")
    List<Long> findAllIds();
}
