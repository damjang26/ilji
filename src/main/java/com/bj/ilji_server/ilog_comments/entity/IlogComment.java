package com.bj.ilji_server.ilog_comments.entity;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog_comment_likes.entity.IlogCommentLike;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ilog_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IlogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ilog_id", nullable = false)
    private ILog ilog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 대댓글 기능 (부모 댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private IlogComment parent;

    // 대댓글 기능 (자식 댓글 목록)
    // 부모 댓글 삭제 시 자식 댓글은 유지해야 하므로 cascade, orphanRemoval 제거
    @OneToMany(mappedBy = "parent")
    private List<IlogComment> children = new ArrayList<>();

    // 논리적 삭제를 위한 필드
    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("0") // DB 기본값과 일치
    private boolean isDeleted = false;

    // 좋아요 수 (조회 성능 최적화용)
    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    // ✅ [추가] 댓글 좋아요 목록과의 관계
    @OneToMany(mappedBy = "ilogComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IlogCommentLike> commentLikes = new ArrayList<>();

    @Builder
    public IlogComment(ILog ilog, UserProfile userProfile, String content, IlogComment parent) {
        this.ilog = ilog;
        this.userProfile = userProfile;
        this.content = content;
        this.parent = parent;
        this.isDeleted = false; // 생성 시에는 항상 false
    }

    // 연관관계 편의 메서드
    public void setParent(IlogComment parent) {
        this.parent = parent;
    }

    /**
     * 댓글을 논리적으로 삭제 처리합니다.
     */
    public void softDelete() {
        this.content = "삭제된 댓글입니다.";
        this.isDeleted = true;
    }

    // ✅ [신규] 좋아요 수 증가 메서드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // ✅ [신규] 좋아요 수 감소 메서드
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
