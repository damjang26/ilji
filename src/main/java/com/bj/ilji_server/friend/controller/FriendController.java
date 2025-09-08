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

    @PostMapping("/{followingId}")
    public ResponseEntity<Void> followUser(@PathVariable Long followingId, @AuthenticationPrincipal User currentUser) {
        friendService.followUser(currentUser.getId(), followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long followingId, @AuthenticationPrincipal User currentUser) {
        friendService.unfollowUser(currentUser.getId(), followingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/following")
    public ResponseEntity<List<FriendResponse>> getFollowingList(@AuthenticationPrincipal User currentUser) {
        List<FriendResponse> followingList = friendService.getFollowingList(currentUser.getId());
        return ResponseEntity.ok(followingList);
    }

    @GetMapping("/followers")
    public ResponseEntity<List<FriendResponse>> getFollowerList(@AuthenticationPrincipal User currentUser) {
        List<FriendResponse> followerList = friendService.getFollowerList(currentUser.getId());
        return ResponseEntity.ok(followerList);
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<FriendshipStatus> getFriendshipStatus(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        FriendshipStatus status = friendService.checkFriendshipStatus(currentUser, otherUser);
        return ResponseEntity.ok(status);
    }
}
