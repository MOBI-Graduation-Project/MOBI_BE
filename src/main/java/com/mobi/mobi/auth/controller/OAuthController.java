package com.mobi.mobi.auth.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.auth.dto.GoogleLoginRequestDTO;
import com.mobi.mobi.auth.dto.GoogleLoginResponseDTO;
import com.mobi.mobi.auth.service.OauthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "인증/인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuthController {

    private final OauthService oauthService;

    @PostMapping("/google")
    @Operation(summary = "구글 소셜 로그인", description = "로그인 성공 시 Refresh Token을 HttpOnly 쿠키로 발급합니다.")
    public ApiResponse<GoogleLoginResponseDTO> googleLogin(
            @RequestBody GoogleLoginRequestDTO request,
            HttpServletResponse response
    ) {
        GoogleLoginResponseDTO tokenResponse = oauthService.loginWithGoogle(
                request.getCode(),
                request.getRedirectUri(),
                request.getCodeVerifier()
        );

        // 1. 리프레시 토큰을 쿠키에 담기 (HttpOnly, Secure)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .maxAge(7 * 24 * 60 * 60) // 7일
                .path("/")
                .secure(true) // HTTPS 환경 필수 (로컬 개발 시 http면 false로 변경 필요할 수 있음)
                .sameSite("None")
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 2. JSON 바디에서는 RefreshToken 제거 (보안 강화)
        tokenResponse.setRefreshToken(null);

        return ApiResponse.onSuccess(SuccessStatus._OK, tokenResponse);
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 갱신 API", description = "쿠키에 있는 Refresh Token을 사용하여 토큰을 재발급합니다.")
    public ApiResponse<GoogleLoginResponseDTO> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ApiResponse.onFailure(ErrorStatus.JWT_REFRESH_TOKEN_NOT_FOUND.getCode(), "리프레시 토큰 쿠키가 없습니다.", null);
        }

        GoogleLoginResponseDTO tokenResponse = oauthService.reissue(refreshToken);

        // 3. 갱신된 리프레시 토큰을 다시 쿠키에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 4. JSON 바디에서는 RefreshToken 제거
        tokenResponse.setRefreshToken(null);

        return ApiResponse.onSuccess(SuccessStatus._OK, tokenResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "쿠키를 초기화하고 DB에서 리프레시 토큰을 삭제합니다.")
    public ApiResponse<Object> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // DB에서 토큰 삭제 (토큰이 유효한 경우만)
        if (refreshToken != null) {
            oauthService.logout(refreshToken);
        }

        // 5. 쿠키 삭제 (Max-Age 0)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}