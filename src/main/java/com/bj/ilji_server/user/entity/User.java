package com.bj.ilji_server.user.entity;

import com.bj.ilji_server.user_profile.entity.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "USERS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile userProfile;

    @Builder
    public User(Long id, String email, String name, String picture, String provider, String providerId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    /**
     * JPA 엔티티 생명주기 콜백 메소드.
     * 데이터베이스에서 User 엔티티가 조회된 직후 자동으로 호출됩니다.
     * UserProfile에 저장된 최신 정보(nickname, profileImage)가 있다면,
     * 이 엔티티의 name과 picture 필드를 해당 값으로 덮어씁니다.
     * 이를 통해 DTO 변환 시 항상 최신 프로필 정보가 반영됩니다.
     */
    @PostLoad
    private void syncProfileToTransientFields() {
        if (this.userProfile != null) {
            if (this.userProfile.getNickname() != null && !this.userProfile.getNickname().isEmpty()) {
                this.name = this.userProfile.getNickname();
            }
            if (this.userProfile.getProfileImage() != null && !this.userProfile.getProfileImage().isEmpty()) {
                this.picture = this.userProfile.getProfileImage();
            }
        }
    }

    // --- UserDetails 구현 --- //

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 우선 모든 사용자에게 "ROLE_USER" 권한 부여
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인이므로 비밀번호는 없음
    }

    @Override
    public String getUsername() {
        return this.email; // Spring Security에서 사용자를 식별하는 주요 값으로 email 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
