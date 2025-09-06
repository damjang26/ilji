package com.bj.ilji_server.user.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GoogleAuthConfig {

    @Value("${google.client.id}")
    private String googleClientIds;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        System.out.println("Loaded Google Client IDs: " + Arrays.toString(googleClientIds.split(","))); // <-- 이 라인 추가
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientIds))
                .build();
    }
}
