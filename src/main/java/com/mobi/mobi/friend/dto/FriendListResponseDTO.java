package com.mobi.mobi.friend.dto;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FriendListResponseDTO {

    @Schema(description = "친구 목록")
    private final List<FriendInfo> friends;

    public FriendListResponseDTO(List<Member> friends) {
        this.friends = friends.stream().map(FriendInfo::new).collect(Collectors.toList());
    }

    @Getter
    public static class FriendInfo {
        @Schema(description = "친구의 Member ID")
        private final Long memberId;
        @Schema(description = "친구의 닉네임")
        private final String nickname;
        @Schema(description = "친구의 프로필 이미지 URL")
        private final String profileImgUrl;
        @Schema(description = "친구의 아바타")
        private final Avatar avatar;

        public FriendInfo(Member member) {
            this.memberId = member.getId();
            this.nickname = member.getNickname();
            this.profileImgUrl = member.getProfileImgUrl();
            this.avatar = member.getAvatar();
        }
    }
}
