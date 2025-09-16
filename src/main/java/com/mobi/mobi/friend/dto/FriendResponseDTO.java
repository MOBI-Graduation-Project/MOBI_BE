package com.mobi.mobi.friend.dto;

import com.mobi.mobi.friend.entity.Friend;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class FriendResponseDTO {

    @Schema(description = "친구 관계 ID")
    private final Long friendId;
    @Schema(description = "요청 보낸 사용자 닉네임")
    private final String fromUserNickname;
    @Schema(description = "요청 받은 사용자 닉네임")
    private final String toUserNickname;
    @Schema(description = "친구 관계 상태")
    private final FriendStatus status;

    public FriendResponseDTO(Friend friend) {
        this.friendId = friend.getId();
        this.fromUserNickname = friend.getFromMember().getNickname();
        this.toUserNickname = friend.getToMember().getNickname();
        this.status = friend.getStatus();
    }
}