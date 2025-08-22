package com.bj.ilji_server.service;

import com.bj.ilji_server.config.jwt.JwtUtil;
import com.bj.ilji_server.dto.AuthResponse;
import com.bj.ilji_server.entity.User;
import com.bj.ilji_server.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse loginWithGoogle(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String providerId = payload.getSubject(); // 구글이 발급한 고유 사용자 ID
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userRepository.findByProviderAndProviderId("google", providerId)
                .map(existingUser -> existingUser.update(name, picture))
                .orElseGet(() -> User.builder()
                        .email(email)
                        .name(name)
                        .picture(picture)
                        .provider("google")
                        .providerId(providerId)
                        .build());

        userRepository.save(user);

        String appToken = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(appToken, new AuthResponse.UserDto(user));
    }
}
