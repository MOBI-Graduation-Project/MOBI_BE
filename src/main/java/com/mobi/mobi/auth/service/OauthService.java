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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.List;
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


    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:}")
    private String GOOGLE_REDIRECT_URI;

    /**
     * 프론트가 준 code(+optional codeVerifier)로
     * 구글 토큰 교환 → OIDC userinfo 조회 → 우리 JWT(access/refresh) 발급 → JSON 반환
     *
     */
    @Transactional
    public GoogleLoginResponseDTO loginWithGoogle(String code, String redirectUri /*unused*/, String codeVerifier) {
        try {
            if (code == null || code.isBlank()) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }


            final String finalRedirectUri = resolveRedirectUri(redirectUri);


            // 1) code → token
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("code", code);
            form.add("client_id", GOOGLE_CLIENT_ID);
            form.add("client_secret", GOOGLE_CLIENT_SECRET);
            form.add("grant_type", "authorization_code");
            form.add("redirect_uri", finalRedirectUri);
            if (codeVerifier != null && !codeVerifier.isBlank()) {
                form.add("code_verifier", codeVerifier);
            }

            log.debug("[GoogleTokenExchange] redirect_uri={}, code_verifier_len={}",
                    finalRedirectUri, (codeVerifier == null ? 0 : codeVerifier.length()));

            Map<String, Object> tokenResp = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (tokenResp == null || tokenResp.get("access_token") == null) {
                log.error("Google token response invalid: {}", tokenResp);
                throw new GeneralException(ErrorStatus.OAUTH_TOKEN_EXCHANGE_FAILED);
            }
            String googleAccessToken = (String) tokenResp.get("access_token");

            // 2) userinfo (OIDC)
            Map<String, Object> userInfo = webClient.get()
                    .uri("https://openidconnect.googleapis.com/v1/userinfo")
                    .headers(h -> h.setBearerAuth(googleAccessToken))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (userInfo == null || userInfo.get("sub") == null) {
                log.error("Google userinfo response invalid: {}", userInfo);
                throw new GeneralException(ErrorStatus.OAUTH_USERINFO_FAILED);
            }

            String sub     = (String) userInfo.get("sub");
            String email   = (String) userInfo.get("email");
            String name    = (String) userInfo.getOrDefault("name", "GoogleUser");
            String picture = (String) userInfo.get("picture");

            // 3) 회원 upsert (이메일 기준)
            UpsertResult up = upsertGoogleMember(sub, email, name, picture);
            Member member   = up.member();
            boolean isNew   = up.isNew();

            // 4) JWT 발급
            String accessToken  = jwtTokenProvider.createAccessToken(String.valueOf(member.getId()));
            String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(member.getId()));

            // 5) refresh 저장 (엔티티/외부저장소에 맞게)
            try {
                // member.setRefreshToken(refreshToken); // 필드가 있으면 사용
                memberRepository.save(member);
            } catch (Exception ignore) {
                // Redis 등 별도 저장소 사용 시:
                // refreshTokenStore.save(member.getId(), refreshToken, jwtTokenProvider.getRefreshTtl());
            }

            // 6) 응답
            return GoogleLoginResponseDTO.builder()
                    .isNewMember(isNew)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .member(member)
                    .build();

        } catch (WebClientResponseException e) {
            final String body = e.getResponseBodyAsString();
            log.warn("[GoogleOAuthError] status={} body={}", e.getRawStatusCode(), body);

            // 원인별 가독성 향상
            if (body != null) {
                if (body.contains("redirect_uri_mismatch")) {
                    log.warn("[Cause] redirect_uri_mismatch: 토큰 교환 redirect_uri 설정 문제");
                    throw new GeneralException(ErrorStatus.OAUTH_REDIRECT_URI_MISMATCH);
                }
                if (body.contains("invalid_grant")) {
                    log.warn("[Cause] invalid_grant: code 재사용/만료 또는 PKCE 불일치 가능");
                    throw new GeneralException(ErrorStatus.OAUTH_INVALID_GRANT);
                }
            }
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login unexpected error", e);
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    private String resolveRedirectUri(String reqRedirectUri) {
        // 프론트가 로컬 테스트용 redirectUri를 보내온 경우 허용
        if (reqRedirectUri != null && !reqRedirectUri.isBlank()) {
            // 로컬 테스트 허용 리스트
            if (reqRedirectUri.startsWith("http://localhost:3000")) {
                return reqRedirectUri;  //프론트 로컬 테스트 허용
            }
        }
        // 그 외엔 prod 고정
        if (GOOGLE_REDIRECT_URI != null && !GOOGLE_REDIRECT_URI.isBlank()) {
            return GOOGLE_REDIRECT_URI; // e.g. https://api.mobi.ai.kr/auth/callback
        }
        throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    @Transactional
    public void logout(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        // refresh 토큰 무효화 (Member 컬럼 보관 가정)
        try {
            member.setRefreshToken(null);
            memberRepository.save(member);
        } catch (Exception ignore) {
            // Redis 등 별도 저장소 사용하는 경우:
            // refreshTokenStore.delete(memberId);
        }
    }


    private UpsertResult upsertGoogleMember(String sub, String email, String name, String picture) {
        // 1) 이메일로 먼저 탐색 (가장 안전)
        Optional<Member> found = memberRepository.findByEmail(email);

        if (found.isPresent()) {
            Member m = found.get();
            // 필요 시 동기화
            if (name   != null) { try { m.setNickname(name); } catch (Exception ignore) {} }
            if (email  != null) { try { m.setEmail(email);   } catch (Exception ignore) {} }
            // if (picture != null) { try { m.setProfileImgUrl(picture); } catch (Exception ignore) {} }
            // if (sub != null) { try { m.setOauthId(sub); } catch (Exception ignore) {} } // 필드가 있을 때만
            return new UpsertResult(memberRepository.save(m), false);
        } else {
            Member m = new Member();
            try { m.setLoginType(LoginType.GOOGLE); } catch (Exception ignore) {}
            try { m.setEmail(email); } catch (Exception ignore) {}
            try { m.setNickname(name); } catch (Exception ignore) {}
            // try { m.setProfileImgUrl(picture); } catch (Exception ignore) {}
            // try { m.setOauthId(sub); } catch (Exception ignore) {}
            return new UpsertResult(memberRepository.save(m), true);
        }
    }

    /** upsert 결과 전용 작은 DTO */
    private record UpsertResult(Member member, boolean isNew) {}
}
