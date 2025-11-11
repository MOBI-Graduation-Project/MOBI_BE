package com.mobi.mobi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequestDTO {

    @Schema(description = "구글 로그인 후 받은 인증 코드", example = "4/0AeaYSH...-redacted")
    private String code;

    @Schema(description = "프론트에서 authorize 요청에 사용한 redirect_uri (없으면 서버 기본값 사용)",
            example = "https://mobi.ai.kr/auth/callback")
    private String redirectUri;

    @Schema(description = "PKCE code_verifier (PKCE 사용 시)", example = "s0meCodeVer1f13r")
    private String codeVerifier;
}
