/*
// src/main/java/com/bj/ilji_server/common/config/WebSocketConfig.java
package com.bj.ilji_server.notification.web_socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 클라이언트가 연결할 WebSocket 엔드포인트.
     * 프론트에서 SockJS를 쓸 계획이면 withSockJS() 유지,
     * 네이티브만 쓸 거면 .withSockJS() 제거해도 됩니다.
     */
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .withSockJS(); // 필요 없으면 삭제
//    }

    /**
     * /app 으로 들어오는 메시지는 애플리케이션 핸들러로 라우팅,
     * /topic 은 브로커가 구독자에게 브로드캐스트.
     *
     * 알림 실시간 전송은 " /topic/notifications/{userId}" 채널을 사용할 예정입니다.
     */
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.setApplicationDestinationPrefixes("/app");
//        registry.enableSimpleBroker("/topic");
//    }
//}
//*/