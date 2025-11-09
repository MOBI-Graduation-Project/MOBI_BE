package com.mobi.mobi.config.security;

import com.mobi.mobi.config.security.jwt.JwtTokenProvider;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository; // MemberRepository 주입

    //로그인 성공후 jwt토큰 발급 역할함->추후 websocket코드 삭제시 필요없음
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 사용자 정보 추출 (Google 기준)
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // DB에서 사용자 조회 또는 새로 생성
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("새로운 사용자입니다. DB에 저장합니다.");
                    return memberRepository.save(
                            Member.builder()
                                    .email(email)
                                    .username(name)
                                    // 여기에 Member 엔티티의 다른 필수 필드가 있다면 기본값으로 설정해야 합니다.
                                    // 예: .role(UserRole.USER)
                                    .build()
                    );
                });

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getId().toString());
        log.info("JWT 토큰이 발급되었습니다: {}", accessToken);


        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"accessToken\": \"" + accessToken + "\"}");
    }
}
