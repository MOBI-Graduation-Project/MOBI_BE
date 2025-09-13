package com.mobi.mobi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleLoginRequestDTO {
    @Schema(description = "구글 로그인 후 받은 인증 코드", example = "4/0AeaYSH...-redacted")
    private String code;
}
