package com.bj.ilji_server.friend.entity;

import com.bj.ilji_server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friend",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "friend_uk",
                        columnNames = {"follower_id", "following_id"}
                )
        })
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Builder
    public Friend(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }
}
