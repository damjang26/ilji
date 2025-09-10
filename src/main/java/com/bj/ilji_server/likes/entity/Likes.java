package com.bj.ilji_server.likes.entity;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        uniqueConstraints = {
                // 한 사용자가 같은 일기에 두 번 좋아요를 누를 수 없도록 복합 유니크 제약조건 설정
                @UniqueConstraint(columnNames = {"user_id", "ilog_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ilog_id", nullable = false)
    private ILog iLog;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Likes(UserProfile userProfile, ILog iLog) {
        this.userProfile = userProfile;
        this.iLog = iLog;
    }
}
