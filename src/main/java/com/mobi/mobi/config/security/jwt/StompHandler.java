package com.mobi.mobi.config.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            log.info("CONNECT - Authorization Header: {}", bearerToken);

            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                String jwtToken = bearerToken.substring(7);
                if (jwtTokenProvider.validateToken(jwtToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);

                    // 기존 코드: 표준적인 방법이지만 현재 동작하지 않음
                    accessor.setUser(authentication);

                    // 세션 속성에 직접 사용자 정보를 저장
                    accessor.getSessionAttributes().put("userPrincipal", authentication);

                    log.info("STOMP Connection Authenticated: {}", authentication.getName());
                } else {
                    log.warn("STOMP Connection failed: Invalid JWT token");
                }
            } else {
                log.warn("STOMP Connection failed: Authorization header is missing or does not start with Bearer");
            }
        }
        return message;
    }
}
