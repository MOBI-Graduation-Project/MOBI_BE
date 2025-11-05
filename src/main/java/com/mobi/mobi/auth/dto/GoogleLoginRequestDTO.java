package com.mobi.mobi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleLoginRequestDTO {
    @Schema(description="구글 인증 코드") private String code;
    @Schema(description="프론트에서 사용한 redirect_uri") private String redirectUri; // SPA면 필수
    @Schema(description="PKCE code_verifier") private String codeVerifier; // PKCE 사용시
}
