package com.mobi.mobi.auth.service;

import com.mobi.mobi.apiPayload.handler.GeneralException;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.auth.dto.GoogleLoginResponseDTO;
import com.mobi.mobi.config.security.jwt.JwtTokenProvider;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.LoginType;
import com.mobi.mobi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OauthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    // 현재 활성화된 프로필 (local, prod 등)을 주입받습니다.
    @Value("${spring.profiles.active}")
    private String activeProfile;

    // yml로부터 local용 redirect-uri를 주입받습니다.
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri.local}")
    private String LOCAL_REDIRECT_URI;

    // yml로부터 prod용 redirect-uri를 주입받습니다.
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri.prod}")
    private String PROD_REDIRECT_URI;


    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code) {

        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        Map<String, Object> tokenResponse = getGoogleAccessToken(decodedCode);
        String googleAccessToken = (String) tokenResponse.get("access_token");
        Map<String, Object> userInfo = getGoogleUserInfo(googleAccessToken);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String profileImgUrl = (String) userInfo.get("picture");

        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        Member member = memberOptional.map(existingMember -> {
            return existingMember.update(name, profileImgUrl);
        }).orElseGet(() -> {
            Member newMember = Member.builder()
                    .email(email)
                    .username(name)
                    .profileImgUrl(profileImgUrl)
                    .loginType(LoginType.GOOGLE)
                    .build();
            newMember.setNickname(name);
            newMember.setIsPrivacyAgreed(false);
            return memberRepository.save(newMember);
        });

        // isSignedUp이 false이면, 추가 정보 입력이 필요한 신규 회원으로 간주
        boolean isNewMember = !member.isSignedUp();

        // 기존 사용자가 재로그인한 경우, 이름과 프로필 사진을 최신 정보로 업데이트
        if (!isNewMember) {
            member.update(name, profileImgUrl);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId().toString());
        member.setRefreshToken(refreshToken);

        return GoogleLoginResponseDTO.builder()
                .isNewMember(isNewMember)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(member)
                .build();
    }

    @Transactional
    public void logout(Long memberId) {
        // 1. memberId를 기반으로 사용자를 찾습니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 해당 사용자의 리프레시 토큰을 DB에서 삭제(null로 업데이트)합니다.
        member.clearRefreshToken();
    }


    private Map<String, Object> getGoogleAccessToken(String code) {
        String tokenUri = "https://oauth2.googleapis.com/token";

        // 3. 현재 프로필에 맞는 redirect-uri를 동적으로 선택합니다.
        String redirectUri = getRedirectUri();

        log.info("--- 구글에 액세스 토큰 요청 ---");
        log.info("Active Profile: {}", activeProfile); // 현재 프로필 로그
        log.info("client_id: {}", GOOGLE_CLIENT_ID);
        log.info("redirect_uri: {}", redirectUri); // 선택된 redirect-uri 로그

        try {
            return webClient.post()
                    .uri(tokenUri, uriBuilder -> uriBuilder
                            .queryParam("code", code)
                            .queryParam("client_id", GOOGLE_CLIENT_ID)
                            .queryParam("client_secret", GOOGLE_CLIENT_SECRET)
                            // 4. 동적으로 선택된 URI를 사용합니다.
                            .queryParam("redirect_uri", redirectUri)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("!!! 구글 서버 에러 응답: {}", responseBody);
            throw new IllegalStateException("Google API 요청 실패. 응답 Body: " + responseBody, e);
        }
    }


    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        String userInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";
        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // --- ⬇️ 3. 이 메서드가 새로 추가되었습니다 ---

    /**
     * 현재 활성화된 프로필(activeProfile)에 따라 적절한 redirect-uri를 반환합니다.
     */
    private String getRedirectUri() {
        // 님의 application.yml 에 'local'로 되어있으므로 'local'을 기준으로 합니다.
        if ("local".equals(activeProfile)) {
            return LOCAL_REDIRECT_URI;
        }

        // 'local'이 아니면 (prod 등) PROD_REDIRECT_URI를 반환합니다.
        return PROD_REDIRECT_URI;
    }
}

