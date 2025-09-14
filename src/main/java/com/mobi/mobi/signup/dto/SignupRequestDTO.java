package com.mobi.mobi.signup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "사용자가 입력한 닉네임", example = "모비모비")
    private String nickname;

    @NotBlank(message = "설문 결과를 입력해주세요.")
    @Schema(description = "사용자가 답변한 설문 결과 ", example = "111")
    private String investmentAnswers;

    @NotNull(message = "약관 동의 여부를 입력해주세요.")
    @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
    private Boolean isPrivacyAgreed;
}
