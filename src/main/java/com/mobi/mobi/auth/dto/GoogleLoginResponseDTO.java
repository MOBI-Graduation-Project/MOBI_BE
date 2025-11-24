package com.mobi.mobi.auth.dto;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
import com.mobi.mobi.member.entity.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleLoginResponseDTO {

    @Schema(description = "최초 가입 유저인지 여부")
    private Boolean isNewMember;

    @Schema(description = "Mobi 서비스 Access Token")
    private String accessToken;

    @Schema(description = "Mobi 서비스 Refresh Token (쿠키로 전달되므로 바디에서는 null)")
    private String refreshToken;

    private MemberInfo member;

    @Builder
    public GoogleLoginResponseDTO(Boolean isNewMember, String accessToken, String refreshToken, Member member) {
        this.isNewMember = isNewMember;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.member = new MemberInfo(member);
    }

    @Getter
    public static class MemberInfo {
        @Schema(description = "멤버 고유 번호")
        private final Long memberId;
        @Schema(description = "이메일")
        private final String email;
        @Schema(description = "닉네임")
        private final String username;
        @Schema(description = "프로필 이미지 URL")
        private final String profileImg;
        @Schema(description = "아바타")
        private final Avatar avatar;
        @Schema(description = "로그인 타입")
        private final LoginType loginType;

        public MemberInfo(Member member) {
            this.memberId = member.getId();
            this.email = member.getEmail();
            this.username = member.getUsername();
            this.profileImg = member.getProfileImgUrl();
            this.avatar = member.getAvatar();
            this.loginType = member.getLoginType();
        }
    }
}