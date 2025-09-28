package com.mobi.mobi.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
// @EnableWebSocketSecurity는 AbstractSecurityWebSocketMessageBrokerConfigurer를 사용하면
// 자동으로 적용되므로 제거해도 됩니다.
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    // configureInbound 메소드를 오버라이드하여 보안 규칙을 설정합니다.
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // STOMP의 CONNECT 요청 등은 누구나 허용
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.HEARTBEAT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.DISCONNECT).permitAll()
                // /sub/** 및 /pub/** 목적지는 인증된 사용자만 접근 가능
                .simpDestMatchers("/sub/**", "/pub/**").authenticated()
                // 그 외 모든 메시지는 거부 (필요에 따라 주석 처리하거나 다른 규칙으로 변경 가능)
                .anyMessage().denyAll();
    }

    // CSRF 보호를 비활성화합니다. (이전 답변 참고)
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}