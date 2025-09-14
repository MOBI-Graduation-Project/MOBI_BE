package com.mobi.mobi.signup.dto;

import com.mobi.mobi.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SignupResponseDTO {
    @Schema(description = "멤버 고유 ID")
    private final Long memberId;
    @Schema(description = "이메일")
    private final String email;
    @Schema(description = "닉네임")
    private final String nickname;
    @Schema(description = "설문조사 결과")
    private final String investmentAnswers;
    @Schema(description = "약관 동의 여부")
    private final Boolean isPrivacyAgreed;
    @Schema(description = "설문 결과에 따라 결정된 아바타 타입", example = "AVATAR_TYPE_1")
    private final String avatar;

    public SignupResponseDTO(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.investmentAnswers = member.getInvestmentAnswers();
        this.isPrivacyAgreed = member.getIsPrivacyAgreed();
        this.avatar = (member.getAvatar() != null) ? member.getAvatar().name() : null;
    }
}
