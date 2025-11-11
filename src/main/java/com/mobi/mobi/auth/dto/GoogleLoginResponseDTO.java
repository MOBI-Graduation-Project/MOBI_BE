
package com.mobi.mobi.auth.dto;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GoogleLoginResponseDTO {

    @Schema(description = "최초 가입 유저인지 여부")
    private final Boolean isNewMember;

    @Schema(description = "Mobi 서비스 Access Token")
    private final String accessToken;

    @Schema(description = "Mobi 서비스 Refresh Token")
    private final String refreshToken;

    private final MemberInfo member;

    @Getter
    public static class MemberInfo {
        @Schema(description = "멤버 고유 번호")
        private final Long memberId;
        @Schema(description = "이메일")
        private final String email;
        @Schema(description = "닉네임")
        private final String username; // API 명세의 nickname을 username으로 매핑
        @Schema(description = "프로필 이미지 URL", nullable = true)
        private final String profileImg;
        @Schema(description = "로그인 타입")
        private final LoginType loginType;

        public MemberInfo(Member member) {
            this.memberId = member.getId();
            this.email = member.getEmail();
            this.username = member.getUsername();
            this.profileImg = member.getProfileImgUrl();
            this.loginType = member.getLoginType();
        }
    }

    @Builder
    public GoogleLoginResponseDTO(Boolean isNewMember, String accessToken, String refreshToken, Member member) {
        this.isNewMember = isNewMember;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.member = new MemberInfo(member);
    }
}