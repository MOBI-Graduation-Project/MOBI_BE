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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code, String redirectUri, String codeVerifier) {
        String finalRedirect = (redirectUri == null || redirectUri.isBlank()) ? null : redirectUri;
        if (finalRedirect == null) {
            throw new IllegalArgumentException("redirectUri is required for front-callback flow");
        }

        Map<String, Object> tokenResponse = getGoogleAccessToken(code, finalRedirect, codeVerifier);
        String googleAccessToken = (String) tokenResponse.get("access_token");
        if (googleAccessToken == null) {
            throw new IllegalStateException("Google access_token 누락. 응답: " + tokenResponse);
        }

        Map<String, Object> userInfo = getGoogleUserInfo(googleAccessToken);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String profileImgUrl = (String) userInfo.get("picture");

        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        Member member = memberOptional.map(existingMember -> existingMember.update(name))
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
            member.update(name);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId().toString());
        member.setRefreshToken(refreshToken);

        // DTO Builder가 내부적으로 Member -> MemberInfo 변환을 수행함
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
    public GoogleLoginResponseDTO reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_EXPIRED);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        Long memberId = Long.parseLong(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String savedRefreshToken = member.getRefreshToken();
        if (savedRefreshToken == null) {
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_NOT_FOUND);
        }

        if (!savedRefreshToken.equals(refreshToken)) {
            log.warn("Refresh Token Mismatch for Member ID: {}", memberId);
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_EXPIRED);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(memberId.toString());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId.toString());

        member.setRefreshToken(newRefreshToken);

        return GoogleLoginResponseDTO.builder()
                .isNewMember(false)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .member(member)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        String resolvedToken = resolveToken(refreshToken);
        Authentication authentication = jwtTokenProvider.getAuthentication(resolvedToken);
        Long memberId = Long.parseLong(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        member.clearRefreshToken();
    }

    private String resolveToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}