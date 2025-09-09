package com.bj.ilji_server.friend.dto;

public enum FriendshipStatus {
    NONE, // 아무 관계 아님
    FOLLOWING, // 내가 상대를 팔로우
    FOLLOWED_BY, // 상대가 나를 팔로우
    MUTUAL // 상호 팔로우
}
