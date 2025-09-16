package com.bj.ilji_server.likes.dto;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import lombok.Getter;

@Getter
public class LikerInfoDTO {
    private final Long userId;
    private final String nickname;
    private final String profileImageUrl;

    public LikerInfoDTO(UserProfile userProfile) {
        this.userId = userProfile.getUserId();
        this.nickname = userProfile.getNickname();
        this.profileImageUrl = userProfile.getProfileImage();
    }
}