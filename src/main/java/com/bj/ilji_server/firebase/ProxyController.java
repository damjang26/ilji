package com.bj.ilji_server.firebase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Controller
@RequiredArgsConstructor
public class ProxyController {

    private final RestTemplate restTemplate;

    @GetMapping("/api/proxy/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        System.out.println("이건 온가 맞아? ---------------------------------------------------------");
        try {
            // 1. 프론트에서 받은 URL로 외부 서버에 이미지 데이터를 요청합니다.
            ResponseEntity<byte[]> response = restTemplate.getForEntity(new URI(imageUrl), byte[].class);

            // 2. 외부 서버의 응답 헤더(Content-Type 등)를 그대로 프론트로 전달합니다.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(response.getHeaders().getContentType());

            // 3. 가져온 이미지 데이터(byte[])와 헤더를 함께 프론트에 반환합니다.

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (URISyntaxException e) { // 잘못된 URL 형식일 경우
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) { // 이미지 가져오기 실패 (404 Not Found 등)
            // 실제 운영에서는 로그를 남기는 것이 좋습니다. e.g. log.error("Proxy request failed: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}