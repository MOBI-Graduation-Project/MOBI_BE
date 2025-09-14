package com.mobi.mobi.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateDescribeRequestDTO {

    @Size(max = 100, message = "한 줄 메시지는 100자 이하로 입력해주세요.")
    @Schema(description = "새로 수정할 한 줄 메시지", example = "새로운 다짐!")
    private String profileDescribe;
}
