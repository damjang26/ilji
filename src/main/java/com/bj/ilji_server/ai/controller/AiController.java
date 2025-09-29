package com.bj.ilji_server.ai.controller;

import com.bj.ilji_server.ai.dto.AiRequest;
import com.bj.ilji_server.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping
    public Mono<String> getAiResponse(@RequestBody AiRequest aiRequest) {
        return aiService.generateContent(aiRequest.getPrompt())
                .flatMap(aiResponse -> {
                    // 응답 구조를 안전하게 탐색하여 텍스트를 추출합니다.
                    String extractedText = Optional.ofNullable(aiResponse)
                            .map(response -> response.getCandidates())
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .map(candidate -> candidate.getContent())
                            .map(content -> content.getParts())
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .map(part -> part.getText())
                            .map(String::trim)
                            .orElse(null);

                    // AI가 IGNORE를 반환하면, 빈 응답을 보내 아무것도 하지 않도록 합니다.
                    if (extractedText != null && "IGNORE".equalsIgnoreCase(extractedText)) {
                        return Mono.empty();
                    }

                    // 추천 태그가 있다면 해당 태그를 반환합니다.
                    return Mono.justOrEmpty(extractedText);
                });
    }
}
