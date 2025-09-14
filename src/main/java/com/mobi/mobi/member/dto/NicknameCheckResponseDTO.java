package com.mobi.mobi.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class NicknameCheckResponseDTO {

    @Schema(description = "확인 요청한 닉네임", example = "모비모비")
    private final String nickname;

    @Schema(description = "닉네임 중복 여부 (true: 중복, false: 사용 가능)", example = "false")
    private final Boolean duplicated;

    public NicknameCheckResponseDTO(String nickname, Boolean duplicated) {
        this.nickname = nickname;
        this.duplicated = duplicated;
    }
}