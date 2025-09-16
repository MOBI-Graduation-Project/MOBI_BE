package com.mobi.mobi.friend.dto;

import com.mobi.mobi.friend.entity.Friend;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FriendRequestResponseDTO {

    @Schema(description = "받은 친구 요청 목록")
    private final List<FriendRequestInfo> friendRequests;

    public FriendRequestResponseDTO(List<Friend> friendRequests) {
        this.friendRequests = friendRequests.stream().map(FriendRequestInfo::new).collect(Collectors.toList());
    }

    @Getter
    public static class FriendRequestInfo {
        @Schema(description = "요청 보낸 사람의 Member ID")
        private final Long fromMemberId;
        @Schema(description = "요청 보낸 사람의 닉네임")
        private final String fromMemberNickname;

        public FriendRequestInfo(Friend friend) {
            this.fromMemberId = friend.getFromMember().getId();
            this.fromMemberNickname = friend.getFromMember().getNickname();
        }
    }
}
