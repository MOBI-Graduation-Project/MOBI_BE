package com.mobi.mobi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 처리를 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 경로(서버 -> 클라이언트)
        registry.enableSimpleBroker("/sub");
        // 클라이언트가 메시지를 보낼 경로(클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결할 때 사용할 엔드포인트
        registry.addEndpoint("/ws-chat") // 예: /ws-chat
                .setAllowedOriginPatterns("*"); // 개발 중에는 모든 오리진을 허용
                //.withSockJS(); // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션
        //->테스트 위해 잠시 주석처리
    }
}
// /ws-chat: 클라이언트가 최초로 WebSocket 연결을 맺는 주소
// /pub: 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로
