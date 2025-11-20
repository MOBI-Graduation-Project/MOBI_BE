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

    // 프론트가 redirectUri를 안 보내면 이 기본값을 사용->로직변경,프론트url강제
    @Value("${google.redirect.uri:}")   // 비우기
    private String GOOGLE_REDIRECT_URI_DEFAULT;

    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code, String redirectUri, String codeVerifier) {
        //String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        String finalRedirect = (redirectUri == null || redirectUri.isBlank())
                ? null
                : redirectUri;
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
                            .profileImgUrl(profileImgUrl) // 신규 가입 시에는 이미지 URL 저장
                            .loginType(LoginType.GOOGLE)
                            .build();
                    newMember.setNickname(name);
                    newMember.setIsPrivacyAgreed(false);
                    return memberRepository.save(newMember);
                });

        boolean isNewMember = !member.isSignedUp();

        if (!isNewMember) {
            member.update(name); // 기존 회원 로그인 시에도 이름만 업데이트
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
    public GoogleLoginResponseDTO reissue(String refreshToken) {
        // 1. 토큰 유효성 검사 (만료 여부 등)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            // 유효하지 않거나 만료된 경우
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_EXPIRED);
        }

        // 2. 토큰에서 유저 정보(Member ID) 추출
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        Long memberId = Long.parseLong(authentication.getName());

        // 3. DB에서 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 4. DB에 저장된 토큰과 요청받은 토큰이 일치하는지 확인 (탈취 방지)
        String savedRefreshToken = member.getRefreshToken();
        if (savedRefreshToken == null) {
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_NOT_FOUND);
        }

        if (!savedRefreshToken.equals(refreshToken)) {
            // 토큰이 일치하지 않음 (다른 곳에서 로그인했거나 탈취 시도 가능성)
            log.warn("Refresh Token Mismatch for Member ID: {}", memberId);
            throw new GeneralException(ErrorStatus.JWT_REFRESH_TOKEN_EXPIRED);
        }

        // 5. 새로운 토큰 쌍 발급 (RTR: Refresh Token Rotation)
        String newAccessToken = jwtTokenProvider.createAccessToken(memberId.toString());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId.toString());

        // 6. DB 업데이트 (새로운 리프레시 토큰 저장)
        member.setRefreshToken(newRefreshToken);

        // 7. 응답 반환
        return GoogleLoginResponseDTO.builder()
                .isNewMember(false) // 갱신이므로 항상 false
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

        // DB의 리프레시 토큰 삭제
        member.clearRefreshToken();
    }
    private String resolveToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}