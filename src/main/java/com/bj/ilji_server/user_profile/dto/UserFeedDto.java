package com.bj.ilji_server.user_profile.dto;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserFeedDto {

    // ✅ [수정] 필드 이름을 UserProfile과 일치시켜 명확성을 높입니다.
    private final Long userId;
    private final String nickname;
    private final String profileImage;

    @Builder
    public UserFeedDto(Long userId, String nickname, String profileImage) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }


    public static UserFeedDto fromEntity(UserProfile userProfile) {
        return UserFeedDto.builder()
                .userId(userProfile.getUserId())
                .nickname(userProfile.getNickname())
                .profileImage(userProfile.getProfileImage())
                .build();
    }
}