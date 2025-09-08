package com.bj.ilji_server.user_profile.entity;

import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_PROFILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id // USER_ID가 Primary Key임을 명시
    private Long id;

    // User 엔티티와 1:1 관계를 맺습니다.
    // @MapsId는 UserProfile의 PK(userId)를 User의 PK와 동일하게 사용하도록 합니다.
    @OneToOne(fetch = FetchType.LAZY) // 프로필 조회 시 User 정보는 항상 필요하므로 EAGER로 설정
    @MapsId
    @JoinColumn(name = "USER_ID") // FK 컬럼을 명시
    private User user;

    @Column(name = "NICKNAME", length = 50)
    private String nickname;

    @Column(name = "BIRTHDATE")
    private LocalDate birthdate; // Oracle의 DATE 타입은 LocalDate와 매핑

    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    @Column(name = "PROFILE_IMAGE", length = 255)
    private String profileImage;

    @Column(name = "BANNER_IMAGE", length = 255)
    private String bannerImage;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "REGION", length = 100)
    private String region;

    @Column(name = "BIO", length = 255)
    private String bio;

    // @Column(name = "IS_PRIVATE") 어노테이션이 Java 필드와 DB 컬럼을 연결해줍니다.
    // Java 필드명(accountPrivate)은 DB 컬럼명과 달라도 되며,
    // 'is' 접두사를 제거하여 프레임워크의 데이터 바인딩 오류를 해결합니다.
    @Column(name = "IS_PRIVATE")
    private boolean accountPrivate;

    @Column(name = "INTERESTS", length = 500)
    private String interests;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Builder
    public UserProfile(User user, String profileImage, String bannerImage) {
        this.id = user.getId(); // @MapsId를 사용하므로 User의 ID를 직접 설정
        this.user = user;
        this.profileImage = profileImage;
        this.bannerImage = bannerImage;
    }

    //== Business Logic (State-Changing Methods) ==//

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public void update(UserProfileUpdateRequest request) {
        // DTO의 필드가 null이 아닐 경우에만 엔티티의 값을 업데이트합니다.
        if (request.getNickname() != null) {
            this.nickname = request.getNickname();
        }
        if (request.getBirthdate() != null) {
            this.birthdate = request.getBirthdate();
        }
        // 이미지 URL은 서비스 레이어에서 직접 처리하므로 여기서는 제외합니다.
        if (request.getGender() != null) {
            this.gender = request.getGender();
        }
        if (request.getRegion() != null) {
            this.region = request.getRegion();
        }
        if (request.getBio() != null) {
            this.bio = request.getBio();
        }
        if (request.getInterests() != null) {
            this.interests = request.getInterests();
        }
        if (request.getPhoneNumber() != null) {
            this.phoneNumber = request.getPhoneNumber();
        }
        this.accountPrivate = request.isPrivate();
    }
}