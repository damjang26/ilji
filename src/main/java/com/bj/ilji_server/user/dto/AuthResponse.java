package com.bj.ilji_server.user.dto;

import com.bj.ilji_server.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String appToken;
    private UserDto user;

    @Getter
    public static class UserDto {
        private final String name;
        private final String email;
        private final String picture;
        private final String providerId;
        private final Long id;

        public UserDto(User user) {
            this.name = user.getName();
            this.email = user.getEmail();
            this.picture = user.getPicture();
            this.providerId = user.getProviderId();
            this.id = user.getId();
        }
    }
}
