package com.bj.ilji_server.friend.dto;

import com.bj.ilji_server.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponse {
    private Long userId;
    private String name;
    private String picture;

    public static FriendResponse from(User user) {
        return FriendResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .picture(user.getPicture())
                .build();
    }
}
