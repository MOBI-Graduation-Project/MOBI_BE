package com.mobi.mobi.auth.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.auth.dto.GoogleLoginRequestDTO;
import com.mobi.mobi.auth.dto.GoogleLoginResponseDTO;
import com.mobi.mobi.auth.dto.TokenReissueRequestDTO;
import com.mobi.mobi.auth.service.OauthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증/인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuthController {

    private final OauthService oauthService;

    @PostMapping("/google")
    @Operation(
            summary = "구글 소셜 로그인",
            description = "클라이언트에서 받은 Authorization Code(+ redirectUri/codeVerifier)로 로그인을 처리하고 JWT 토큰을 발급합니다."
    )
    public ApiResponse<GoogleLoginResponseDTO> googleLogin(@RequestBody GoogleLoginRequestDTO request) {
        GoogleLoginResponseDTO response = oauthService.loginWithGoogle(
                request.getCode(),
                request.getRedirectUri(),
                request.getCodeVerifier()
        );
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @PostMapping("/reissue")
    @Operation(
            summary = "토큰 갱신 API",
            description = "Refresh Token을 사용하여 Access Token과 Refresh Token을 모두 재발급합니다. (RTR 방식)"
    )
    public ApiResponse<GoogleLoginResponseDTO> reissue(@RequestBody TokenReissueRequestDTO request) {
        GoogleLoginResponseDTO response = oauthService.reissue(request.getRefreshToken());
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 DB에서 삭제합니다. (Access Token 만료 여부와 관계없이 로그아웃 가능)"
    )
    public ApiResponse<Object> logout(@RequestBody TokenReissueRequestDTO request) {
        oauthService.logout(request.getRefreshToken());
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}
