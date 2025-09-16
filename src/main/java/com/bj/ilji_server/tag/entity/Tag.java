package com.bj.ilji_server.tag.entity;

import com.bj.ilji_server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagVisibility visibility = TagVisibility.PRIVATE;

    @Column(nullable = false)
    private Long position;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Tag(User user, String label, String color, TagVisibility visibility) {
        this.user = user;
        this.label = label;
        this.color = color;
        this.visibility = (visibility != null) ? visibility : TagVisibility.PRIVATE;
        this.position = System.currentTimeMillis(); // 생성 시 타임스탬프를 위치값으로 저장
    }

    // 드래그앤드랍 순서 변경을 위한 메소드
    public void updatePosition(Long position) {
        this.position = position;
    }

    public void updateVisibility(TagVisibility visibility) {
        this.visibility = visibility;
    }

    public void update(com.bj.ilji_server.tag.dto.TagUpdateRequest request) {
        if (request.getLabel() != null) {
            this.label = request.getLabel();
        }
        if (request.getColor() != null) {
            this.color = request.getColor();
        }
        if (request.getVisibility() != null) {
            this.visibility = request.getVisibility();
        }
    }
}
