package com.mobi.mobi.member.dto;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.RelationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberSearchResponseDTO {

    @Schema(description = "검색된 사용자 목록")
    private List<MemberInfo> searchResults;

    @Getter
    @Builder
    public static class MemberInfo {
        @Schema(description = "사용자 ID")
        private Long memberId;

        @Schema(description = "닉네임")
        private String nickname;

        @Schema(description = "프로필 이미지 URL")
        private String profileImgUrl;

        @Schema(description = "아바타")
        private String avatar;

        @Schema(description = "로그인한 사용자와의 관계")
        private RelationStatus relationStatus;
    }

    // Member와 RelationStatus를 MemberInfo로 변환하는 정적 팩토리 메서드
    public static MemberInfo of(Member member, RelationStatus status) {
        return MemberInfo.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .avatar(member.getAvatar() != null ? member.getAvatar().name() : null)
                .relationStatus(status)
                .build();
    }
}
