package com.bj.ilji_server.user_profile.repository;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // JpaRepository를 상속받는 것만으로 기본적인 CRUD(Create, Read, Update, Delete) 기능이 자동으로 구현됩니다.
}
