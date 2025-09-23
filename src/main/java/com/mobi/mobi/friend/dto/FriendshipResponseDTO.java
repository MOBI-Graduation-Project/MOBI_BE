package com.mobi.mobi.friend.dto;

import com.mobi.mobi.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FriendshipResponseDTO {

    @Schema(description = "나의 친구 목록 (상태: ACCEPTED)")
    private final List<FriendInfo> friendList;

    @Schema(description = "내가 받은 친구 요청 목록 (상태: PENDING)")
    private final List<FriendRequestInfo> friendRequestList;

    public FriendshipResponseDTO(List<Member> friends, List<Member> requesters) {
        this.friendList = friends.stream().map(FriendInfo::new).collect(Collectors.toList());
        this.friendRequestList = requesters.stream().map(FriendRequestInfo::new).collect(Collectors.toList());
    }

    @Getter
    public static class FriendInfo {

        @Schema(description = "친구의 Member ID")
        private final Long memberId;
        @Schema(description = "친구의 닉네임")
        private final String nickname;
        @Schema(description = "친구의 아바타 식별자")
        private final String avatar;
        @Schema(description = "친구의 프로필 사진")
        private final String profileImgUrl;
        @Schema(description = "친구의 한줄 메시지")
        private final String profileDescribe;

        public FriendInfo(Member member) {
            this.memberId = member.getId();
            this.nickname = member.getNickname();
            this.avatar = (member.getAvatar() != null) ? member.getAvatar().name() : null;
            this.profileImgUrl = member.getProfileImgUrl();
            this.profileDescribe = member.getProfileDescribe();
        }
    }

    @Getter
    public static class FriendRequestInfo {
        @Schema(description = "요청 보낸 사람의 Member ID")
        private final Long fromMemberId;
        @Schema(description = "요청 보낸 사람의 닉네임")
        private final String fromMemberNickname;
        @Schema(description = "요청 보낸 사람의 프로필 사진")
        private final String fromMemberProfileImgUrl;
        @Schema(description = "요청 보낸 사람의 한줄 메시지")
        private final String fromMemberProfileDescribe;

        public FriendRequestInfo(Member member) {
            this.fromMemberId = member.getId();
            this.fromMemberNickname = member.getNickname();
            this.fromMemberProfileImgUrl = member.getProfileImgUrl();
            this.fromMemberProfileDescribe = member.getProfileDescribe();
        }
    }
}




