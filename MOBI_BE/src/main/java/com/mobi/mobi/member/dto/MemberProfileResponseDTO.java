package com.mobi.mobi.member.dto;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.RelationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponseDTO {

    @Schema(description = "멤버 고유 ID")
    private final Long memberId;

    @Schema(description = "이메일")
    private final String email;

    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL (구글 프로필 사진)")
    private final String profileImgUrl;

    @Schema(description = "설문 결과에 따른 아바타 식별자")
    private final String avatar;

    @Schema(description = "한 줄 메시지")
    private final String profileDescribe;

    @Schema(description = "로그인한 사용자와의 관계")
    private final RelationStatus relationStatus;

    public static MemberProfileResponseDTO of(Member member, RelationStatus status) {
        return MemberProfileResponseDTO.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .avatar(member.getAvatar() != null ? member.getAvatar().name() : null)
                .profileDescribe(member.getProfileDescribe())
                .relationStatus(status)
                .build();
    }
}