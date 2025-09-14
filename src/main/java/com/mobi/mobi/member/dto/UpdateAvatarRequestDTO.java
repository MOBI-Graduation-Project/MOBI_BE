package com.mobi.mobi.member.dto;

import com.mobi.mobi.member.entity.enums.Avatar;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAvatarRequestDTO {

    @NotNull(message = "변경할 아바타를 선택해주세요.")
    @Schema(description = "새로 수정할 아바타 타입", example = "AVATAR_TYPE_1")
    private Avatar avatar;
}
