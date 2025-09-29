package com.bj.ilji_server.friend.repository;

import com.bj.ilji_server.friend.entity.Friend;
import com.bj.ilji_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    // ✅ [신규] User ID를 기반으로 친구 관계 존재 여부를 확인합니다.
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Friend> findAllByFollower(User follower); // 내가 팔로우하는 친구들 목록

    List<Friend> findAllByFollowing(User following); // 나를 팔로우하는 친구들 목록

    long countByFollower(User follower); // 내가 팔로우하는 친구 수

    long countByFollowing(User following); // 나를 팔로우하는 친구 수
}
