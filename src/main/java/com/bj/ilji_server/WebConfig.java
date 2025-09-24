package com.bj.ilji_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 허용
                .allowedOrigins(
                        "http://localhost:8081", // Expo 개발 서버 기본 포트
                        "http://localhost:19000", // Expo Go 앱이 사용하는 포트
                        "http://localhost:19001", // Expo Go 앱이 사용하는 다른 포트
                        "http://10.0.2.2:8081", // Android 에뮬레이터에서 Expo 개발 서버 접근 시
                        "http://10.0.2.2:8090", // Android 에뮬레이터에서 백엔드 직접 접근 시
                        "http://localhost:8090" // 백엔드 자체 주소
                        // 여기에 개발 머신의 실제 IP 주소 (예: "http://192.168.0.10:8081")를 추가할 수도 있습니다.
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
