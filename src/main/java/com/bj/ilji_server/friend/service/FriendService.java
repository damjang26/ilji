package com.bj.ilji_server.friend.service;

import com.bj.ilji_server.friend.dto.FriendResponse;
import com.bj.ilji_server.friend.dto.FriendshipStatus;
import com.bj.ilji_server.friend.entity.Friend;
import com.bj.ilji_server.friend.repository.FriendRepository;
import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.bj.ilji_server.user_profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.bj.ilji_server.notification.packing.NotificationComposer;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationComposer  notificationComposer;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));

        if (friendRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("Already following this user");
        }

        Friend friend = Friend.builder()
                .follower(follower)
                .following(following)
                .build();

        friendRepository.save(friend);

//        // Create notification
//        notificationService.createFollowNotification(follower, following);
        // 표시 이름(닉네임 우선, 없으면 이메일 로컬파트)
        UserProfile followerProfile = userProfileRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("UserProfile not found: " + followerId));

        String displayName;
        if (followerProfile.getNickname() != null && !followerProfile.getNickname().isBlank()) {
            displayName = followerProfile.getNickname();
        } else if (follower.getEmail() != null) {
            int at = follower.getEmail().indexOf('@');
            displayName = at > 0 ? follower.getEmail().substring(0, at) : follower.getEmail();
        } else {
            displayName = "알 수 없음";
        }

        notificationComposer.followRequested(
                following.getId(),   // targetUserId = 수신자
                follower.getId(),    // followerId   = 발신자
                displayName          // followerName = 화면 표시 이름(닉네임 폴백)
        );
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));

        Friend friend = friendRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new IllegalStateException("Not following this user"));

        friendRepository.delete(friend);
    }

    public List<FriendResponse> getFollowingList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendRepository.findAllByFollower(user).stream()
                .map(Friend::getFollowing)
                .map(FriendResponse::from)
                .collect(Collectors.toList());
    }

    public List<FriendResponse> getFollowerList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendRepository.findAllByFollowing(user).stream()
                .map(Friend::getFollower)
                .map(FriendResponse::from)
                .collect(Collectors.toList());
    }

    public FriendshipStatus checkFriendshipStatus(User user1, User user2) {
        boolean user1FollowsUser2 = friendRepository.existsByFollowerAndFollowing(user1, user2);
        boolean user2FollowsUser1 = friendRepository.existsByFollowerAndFollowing(user2, user1);

        if (user1FollowsUser2 && user2FollowsUser1) {
            return FriendshipStatus.MUTUAL;
        } else if (user1FollowsUser2) {
            return FriendshipStatus.FOLLOWING;
        } else if (user2FollowsUser1) {
            return FriendshipStatus.FOLLOWED_BY;
        } else {
            return FriendshipStatus.NONE;
        }
    }
}
