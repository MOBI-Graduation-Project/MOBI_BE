package com.mobi.mobi.auth.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.auth.dto.GoogleLoginRequestDTO;
import com.mobi.mobi.auth.dto.GoogleLoginResponseDTO;
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
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
@RequestMapping("/auth")
public class OAuthController {
    private final OauthService oauthService;

    @PostMapping("/google")
    @Operation(summary = "구글 소셜 로그인", description = "클라이언트에서 받은 Authorization Code로 로그인을 처리하고 JWT 토큰을 발급합니다.")
    public ApiResponse<GoogleLoginResponseDTO> googleLogin(@RequestBody GoogleLoginRequestDTO request) {
        GoogleLoginResponseDTO response = oauthService.loginWithGoogle(request.getCode());
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "서버에 저장된 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Object> logout(@AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        oauthService.logout(memberId);

        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}
