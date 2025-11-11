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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
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

    // 프론트가 redirectUri를 안 보내면 이 기본값을 사용
    @Value("${google.redirect.uri:https://mobi.ai.kr/auth/callback}")
    private String GOOGLE_REDIRECT_URI_DEFAULT;

    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code, String redirectUri, String codeVerifier) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        String finalRedirect = (redirectUri == null || redirectUri.isBlank())
                ? GOOGLE_REDIRECT_URI_DEFAULT
                : redirectUri;

        Map<String, Object> tokenResponse = getGoogleAccessToken(decodedCode, finalRedirect, codeVerifier);
        String googleAccessToken = (String) tokenResponse.get("access_token");
        if (googleAccessToken == null) {
            throw new IllegalStateException("Google access_token 누락. 응답: " + tokenResponse);
        }

        Map<String, Object> userInfo = getGoogleUserInfo(googleAccessToken);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String profileImgUrl = (String) userInfo.get("picture");

        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        Member member = memberOptional.map(existingMember -> existingMember.update(name, profileImgUrl))
                .orElseGet(() -> {
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

        boolean isNewMember = !member.isSignedUp();

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

    private Map<String, Object> getGoogleAccessToken(String code, String redirectUri, String codeVerifier) {
        String tokenUri = "https://oauth2.googleapis.com/token";
        log.info("--- 구글에 액세스 토큰 요청 ---");
        log.info("client_id: {}", GOOGLE_CLIENT_ID);
        log.info("redirect_uri(final): {}", redirectUri);

        try {
            BodyInserters.FormInserter<String> form = BodyInserters
                    .fromFormData("code", code)
                    .with("client_id", GOOGLE_CLIENT_ID)
                    .with("client_secret", GOOGLE_CLIENT_SECRET)
                    .with("redirect_uri", redirectUri)
                    .with("grant_type", "authorization_code");

            if (codeVerifier != null && !codeVerifier.isBlank()) {
                form = form.with("code_verifier", codeVerifier);
            }

            return webClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("!!! 구글 서버 에러 응답: status={}, body={}", e.getRawStatusCode(), responseBody);
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

    @Transactional
    public void logout(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        member.clearRefreshToken();
    }
}