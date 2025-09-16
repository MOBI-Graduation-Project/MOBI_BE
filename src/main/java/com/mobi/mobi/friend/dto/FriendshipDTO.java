package com.mobi.mobi.friend.dto;

import com.mobi.mobi.friend.entity.Friend;
import com.mobi.mobi.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class FriendshipDTO {

    @Schema(description = "나의 친구 목록 (상태: ACCEPTED)")
    private final List<FriendInfo> friendList;

    @Schema(description = "내가 받은 친구 요청 목록 (상태: PENDING)")
    private final List<FriendRequestInfo> friendRequestList;

    public FriendshipDTO(List<Friend> acceptedFriends, List<Friend> pendingRequests, Long myId) {
        // 친구 목록 생성 (관계에서 '상대방' 정보만 추출)
        this.friendList = acceptedFriends.stream()
                .map(friend -> {
                    Member friendMember = Objects.equals(friend.getFromMember().getId(), myId) ? friend.getToMember() : friend.getFromMember();
                    return new FriendInfo(friendMember);
                })
                .collect(Collectors.toList());

        // 받은 친구 요청 목록 생성
        this.friendRequestList = pendingRequests.stream()
                .map(FriendRequestInfo::new)
                .collect(Collectors.toList());
    }

    // 친구 정보를 담는 내부 클래스
    @Getter
    public static class FriendInfo {
        @Schema(description = "친구의 Member ID")
        private final Long memberId;
        @Schema(description = "친구의 닉네임")
        private final String nickname;
        @Schema(description = "친구의 아바타 식별자")
        private final String avatar;

        public FriendInfo(Member member) {
            this.memberId = member.getId();
            this.nickname = member.getNickname();
            this.avatar = (member.getAvatar() != null) ? member.getAvatar().name() : null;
        }
    }

    // 친구 요청 정보를 담는 내부 클래스
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
