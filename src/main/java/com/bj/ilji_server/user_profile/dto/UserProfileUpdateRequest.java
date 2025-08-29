package com.bj.ilji_server.user_profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private LocalDate birthdate;
    private String phoneNumber;
    private String bannerImage;
    private String gender;
    private String region;
    private String bio;
    // Jackson에게 JSON 키 'isPrivate'와 이 필드를 명확하게 연결해주는 이름표 역할
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private String interests;
}