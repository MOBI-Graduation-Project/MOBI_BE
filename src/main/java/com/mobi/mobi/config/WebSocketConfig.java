package com.mobi.mobi.config;

import com.mobi.mobi.config.security.jwt.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 처리를 활성화합니다.
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

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
                .setAllowedOriginPatterns("*") // 개발 중에는 모든 오리진을 허용
                .withSockJS(); // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // StompHandler를 인터셉터로 등록하여 JWT 인증 처리
        registration.interceptors(stompHandler);
    }
}
// /ws-chat: 클라이언트가 최초로 WebSocket 연결을 맺는 주소
// /pub: 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로
