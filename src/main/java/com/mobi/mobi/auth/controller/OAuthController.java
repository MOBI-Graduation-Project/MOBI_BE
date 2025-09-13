package com.mobi.mobi.auth.controller;

import com.mobi.mobi.auth.dto.GoogleLoginRequestDTO;
import com.mobi.mobi.auth.dto.GoogleLoginResponseDTO;
import com.mobi.mobi.auth.service.OauthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<GoogleLoginResponseDTO> googleLogin(@RequestBody GoogleLoginRequestDTO request) {
        GoogleLoginResponseDTO response = oauthService.loginWithGoogle(request.getCode());
        return ResponseEntity.ok(response);

    }
}
