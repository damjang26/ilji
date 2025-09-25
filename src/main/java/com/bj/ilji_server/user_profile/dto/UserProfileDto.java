package com.bj.ilji_server.user_profile.dto;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private Long id;
    private String nickname;
    private String profileImage;

    public static UserProfileDto fromEntity(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        return UserProfileDto.builder()
                .id(userProfile.getUserId())
                .nickname(userProfile.getNickname())
                .profileImage(userProfile.getProfileImage())
                .build();
    }
}