package com.bj.ilji_server.user_profile.entity;

import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_PROFILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id // USER_ID가 Primary Key임을 명시
    private Long userId;

    // User 엔티티와 1:1 관계를 맺습니다.
    // @MapsId는 UserProfile의 PK(userId)를 User의 PK와 동일하게 사용하도록 합니다.
    @OneToOne(fetch = FetchType.EAGER) // 프로필 조회 시 User 정보는 항상 필요하므로 EAGER로 설정
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

    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간을 자동으로 저장
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 업데이트될 때마다 현재 시간을 자동으로 저장
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Builder
    public UserProfile(User user) {
        this.user = user;
        // SQL 주석에 명시된 대로, 기본 닉네임과 프로필 이미지를
        // User 엔티티의 정보로 초기화합니다.
        this.nickname = user.getName();
        this.profileImage = user.getPicture();
    }

    public void update(UserProfileUpdateRequest request) {
        this.nickname = request.getNickname();
        this.birthdate = request.getBirthdate();
        this.phoneNumber = request.getPhoneNumber();
        this.bannerImage = request.getBannerImage();
        this.gender = request.getGender();
        this.region = request.getRegion();
        this.bio = request.getBio();
//        this.accountPrivate = request.isPrivate(); // DTO와 프론트엔드에서도 필드명을 통일해야 합니다.

        // [로그 3] DB에 저장되기 직전, 실제 필드 값이 바뀌는 순간 확인
        System.out.println("✅ [Entity] 값 변경 전: " + this.accountPrivate + " | DTO에서 받은 값: " + request.isPrivate());
        this.accountPrivate = request.isPrivate();
        System.out.println("✅ [Entity] 값 변경 후: " + this.accountPrivate);

        this.interests = request.getInterests();
    }
}