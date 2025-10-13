package com.bj.ilji_server.ai.service;

import com.bj.ilji_server.ai.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    // --- AI 역할을 정의하는 시스템 프롬프트 ---
    private static final String SYSTEM_PROMPT = """
            당신은 채팅 메시지를 분석하여 약속이나 일정을 잡는 상황인지 판단하고, 그에 맞는 카테고리 태그를 추천하는 똑똑한 비서입니다.

            주어진 메시지를 분석해서 다음 규칙에 따라 응답하세요.

            1.  만약 메시지가 약속, 모임, 일정 잡기와 관련이 있다면, 그 내용을 대표할 수 있는 일반적이고 재사용 가능한 "카테고리 태그"를 한두 단어의 간결한 한글로 추천해주세요. 응답은 추천 태그 자체만 포함해야 합니다. (예: "점심 약속", "저녁 식사", "커피 약속", "영화 관람", "스터디 모임", "회의")
            2.  만약 메시지가 약속이나 일정과 관련 없는 단순한 일상 대화라면, 다른 어떤 말도 하지 말고 반드시 `IGNORE` 라는 단어 하나만 응답하세요.

            ---
            [예시]
            입력: 다음주 화요일 5시에 밥한끼 어때요?
            출력: 식사 약속

            입력: 우리 내일 5시 커피 마시기 어때?
            출력: 커피 약속

            입력: 금요일 저녁에 다같이 스터디 진행하시죠
            출력: 스터디 모임

            입력: 오늘 날씨 진짜 좋다
            출력: IGNORE

            입력: 주말에 영화 볼 사람?
            출력: 영화 관람

            입력:
            """ ;
    // -------------------------------------

    private final WebClient webClient;
    private final String vertexAiEndpoint;

    private String cachedToken;
    private Instant tokenExpiryTime;

    public AiService(WebClient.Builder webClientBuilder,
                     @Value("${vertex.ai.endpoint}") String vertexAiEndpoint) {
        this.webClient = webClientBuilder.build();
        this.vertexAiEndpoint = vertexAiEndpoint;
    }

    public Mono<AiResponse> generateContent(String userPrompt) {
        // 시스템 프롬프트와 사용자 메시지를 결합
        String finalPrompt = SYSTEM_PROMPT + userPrompt;

        return getAccessToken().flatMap(token -> {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of("text", finalPrompt)
                                    )
                            )
                    )
            );

            return webClient.post()
                    .uri(vertexAiEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AiResponse.class);
        });
    }

    // --- 캐싱 로직이 추가된 getAccessToken 메소드 ---
    private Mono<String> getAccessToken() {
        // 1. 캐시된 토큰이 유효한지 확인
        if (cachedToken != null && Instant.now().isBefore(tokenExpiryTime)) {
            return Mono.just(cachedToken);
        }

        // 2. 토큰이 유효하지 않으면 새로 발급
        return Mono.fromCallable(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("/Users/jung/Desktop/JHW/google-cloud-sdk/bin/gcloud", "auth", "print-access-token");
                Process process = processBuilder.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    // 표준 에러 스트림을 읽어 더 자세한 오류 메시지를 포함할 수 있습니다.
                    StringBuilder errorOutput = new StringBuilder();
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorOutput.append(line).append("\n");
                        }
                    }
                    throw new RuntimeException("Failed to get gcloud access token, exit code: " + exitCode + ", error: " + errorOutput);
                }
                return output.toString().trim();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error getting gcloud access token", e);
            }
        }).doOnSuccess(token -> {
            // 3. 새로 발급받은 토큰을 캐시에 저장
            this.cachedToken = token;
            // Google 토큰은 보통 1시간(3600초)동안 유효합니다. 안전하게 55분(3300초)으로 설정합니다.
            this.tokenExpiryTime = Instant.now().plusSeconds(3300);
            System.out.println("New access token has been fetched and cached.");
        });
    }
    // -----------------------------------------
}