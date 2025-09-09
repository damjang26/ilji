package com.bj.ilji_server.user_profile.dto;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserProfileResponse {

    private final Long userId;
    private final String email; // User 엔티티에서 가져옴
    private final String name; // User 엔티티에서 가져옴
    private final String nickname;
    private final LocalDate birthdate;
    private final String phoneNumber;
    private final String profileImage;
    private final String bannerImage;
    private final String gender;
    private final String region;
    private final String bio;
    private final boolean isPrivate;
    private final String interests;

    // Entity를 DTO로 변환하는 정적 팩토리 메소드
    public static UserProfileResponse from(UserProfile userProfile) {
        return new UserProfileResponse(userProfile);
    }

    private UserProfileResponse(UserProfile userProfile) {
        this.userId = userProfile.getUserId();
        this.email = userProfile.getUser().getEmail(); // 연관된 User 정보 사용
        this.name = userProfile.getUser().getName();
        this.nickname = userProfile.getNickname();
        this.birthdate = userProfile.getBirthdate();
        this.phoneNumber = userProfile.getPhoneNumber();
        this.profileImage = userProfile.getProfileImage();
        this.bannerImage = userProfile.getBannerImage();
        this.gender = userProfile.getGender();
        this.region = userProfile.getRegion();
        this.bio = userProfile.getBio();
        this.isPrivate = userProfile.isAccountPrivate();
        this.interests = userProfile.getInterests();
    }
}