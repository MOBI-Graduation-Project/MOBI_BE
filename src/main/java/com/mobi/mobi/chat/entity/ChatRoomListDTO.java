package com.mobi.mobi.chat.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatRoomListDTO {
    private Long roomId; // 채팅방 ID
    private String roomName; // 채팅방 이름 (1:1 채팅의 경우 상대방 이름)
    private String lastMessage; // 마지막 메시지 내용
    private LocalDateTime lastMessageSentAt; // 마지막 메시지 시간
    private Long unreadCount; // 안 읽은 메시지 개수
    private String otherMemberProfileImage; // 1:1 채팅의 경우 상대방 프로필 이미지
}
