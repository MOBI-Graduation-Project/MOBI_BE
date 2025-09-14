package com.mobi.mobi.auth.service;

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

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;


    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code) {

        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        Map<String, Object> tokenResponse = getGoogleAccessToken(decodedCode);
        String accessToken = (String) tokenResponse.get("access_token");
        Map<String, Object> userInfo = getGoogleUserInfo(accessToken);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String profileImgUrl = (String) userInfo.get("picture");

        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        boolean isNewMember = memberOptional.isEmpty();
        Member member = memberOptional.map(existingMember -> {
            return existingMember.update(name, profileImgUrl);
        }).orElseGet(() -> {
            Member newMember = Member.builder()
                    .email(email)
                    .username(name)
                    .profileImgUrl(profileImgUrl)
                    .loginType(LoginType.GOOGLE)
                    .build();
            newMember.setIsPrivacyAgreed(false);
            return memberRepository.save(newMember);
        });

        String serviceAccessToken = jwtTokenProvider.createToken(member.getId().toString());

        return GoogleLoginResponseDTO.builder()
                .isNewMember(isNewMember).accessToken(serviceAccessToken).member(member)
                .build();
    }


    private Map<String, Object> getGoogleAccessToken(String code) {
        String tokenUri = "https://oauth2.googleapis.com/token";

        log.info("--- 구글에 액세스 토큰 요청 ---");
        log.info("client_id: {}", GOOGLE_CLIENT_ID);
        log.info("redirect_uri: {}", GOOGLE_REDIRECT_URI);

        try {
            return webClient.post()
                    .uri(tokenUri, uriBuilder -> uriBuilder
                            .queryParam("code", code)
                            .queryParam("client_id", GOOGLE_CLIENT_ID)
                            .queryParam("client_secret", GOOGLE_CLIENT_SECRET)
                            .queryParam("redirect_uri", GOOGLE_REDIRECT_URI)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("!!! 구글 서버 에러 응답: {}", responseBody);

            // 에러 메시지에 구글의 응답을 포함시켜서 다시 던집니다.
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
}