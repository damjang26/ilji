package com.bj.ilji_server.ilog_comment_likes.entity;

import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ilog_comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_comment_like",
                        columnNames = {"comment_id", "user_id"}
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IlogCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private IlogComment ilogComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public IlogCommentLike(IlogComment ilogComment, UserProfile userProfile) {
        this.ilogComment = ilogComment;
        this.userProfile = userProfile;
    }
}