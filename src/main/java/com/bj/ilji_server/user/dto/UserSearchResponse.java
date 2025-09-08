package com.bj.ilji_server.user.dto;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResponse {
    private Long userId;
    private String name;
    private String picture;

    public static UserSearchResponse from(UserProfile userProfile) {
        return UserSearchResponse.builder()
                .userId(userProfile.getUserId())
                .name(userProfile.getNickname()) // UserProfile의 nickname을 name으로 매핑
                .picture(userProfile.getProfileImage()) // UserProfile의 profileImage를 picture로 매핑
                .build();
    }
}
