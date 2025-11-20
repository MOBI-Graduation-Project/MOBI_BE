package com.mobi.mobi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDTO {
    @Schema(description = "만료된 Access Token 대신 사용할 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}