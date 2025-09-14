package com.bj.ilji_server.friend.controller;

import com.bj.ilji_server.friend.dto.FriendResponse;
import com.bj.ilji_server.friend.dto.FriendshipStatus;
import com.bj.ilji_server.friend.service.FriendService;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository; // For status check

    private User getAuthenticatedUser(User user) {
        if (user == null) {
            return userRepository.findById(4L)
                    .orElseThrow(() -> new RuntimeException("테스트 유저(ID=4)를 찾을 수 없습니다. DB를 확인해주세요."));
        }
        return user;
    }

    @PostMapping("/{followingId}")
    public ResponseEntity<Void> followUser(@PathVariable Long followingId, @AuthenticationPrincipal User currentUser) {
        User user = getAuthenticatedUser(currentUser);
        friendService.followUser(user.getId(), followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long followingId, @AuthenticationPrincipal User currentUser) {
        User user = getAuthenticatedUser(currentUser);
        friendService.unfollowUser(user.getId(), followingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/following")
    public ResponseEntity<List<FriendResponse>> getFollowingList(@AuthenticationPrincipal User currentUser) {
        User user = getAuthenticatedUser(currentUser);
        List<FriendResponse> followingList = friendService.getFollowingList(user.getId());
        return ResponseEntity.ok(followingList);
    }

    @GetMapping("/followers")
    public ResponseEntity<List<FriendResponse>> getFollowerList(@AuthenticationPrincipal User currentUser) {
        User user = getAuthenticatedUser(currentUser);
        List<FriendResponse> followerList = friendService.getFollowerList(user.getId());
        return ResponseEntity.ok(followerList);
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<FriendshipStatus> getFriendshipStatus(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        User user = getAuthenticatedUser(currentUser);
        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        FriendshipStatus status = friendService.checkFriendshipStatus(user, otherUser);
        return ResponseEntity.ok(status);
    }
}
