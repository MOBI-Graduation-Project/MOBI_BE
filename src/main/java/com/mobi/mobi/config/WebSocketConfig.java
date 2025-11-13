package com.mobi.mobi.config;

import com.mobi.mobi.config.security.jwt.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        // 운영
                        "https://mobi.ai.kr",
                        "https://www.mobi.ai.kr",
                        "https://api.mobi.ai.kr",
                        // 로컬
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://127.0.0.1:3000",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
                )
                .withSockJS(); // /ws, /ws/info 등 핸드셰이크 경로 자동 포함
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}

// /ws-chat: 클라이언트가 최초로 WebSocket 연결을 맺는 주소
// /pub: 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로
