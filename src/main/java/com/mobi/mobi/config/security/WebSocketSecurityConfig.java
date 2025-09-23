package com.mobi.mobi.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    //websocket 테스트용 임시코드
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // 클라이언트가 서버로 메시지를 보낼 때 사용하는 "/pub" 경로에 대해 모두 허용
                .simpDestMatchers("/pub/**").permitAll()
                // 클라이언트가 서버를 구독할 때 사용하는 "/sub" 경로에 대해 모두 허용
                .simpSubscribeDestMatchers("/sub/**").permitAll()
                // 그 외 모든 메시지에 대해서도 일단 허용 (필요에 따라 더 세분화된 설정 가능)
                .anyMessage().permitAll();
    }

    /**
     * CSRF 토큰 검사를 비활성화합니다.
     * Postman과 같은 외부 클라이언트에서의 테스트를 쉽게 하기 위함입니다.
     * 기본적으로 Spring Security는 WebSocket 연결 시 CSRF 토큰을 요구합니다.
     */
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
