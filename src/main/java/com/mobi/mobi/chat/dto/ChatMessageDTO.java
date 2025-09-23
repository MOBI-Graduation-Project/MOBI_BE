package com.mobi.mobi.chat.dto;

import com.mobi.mobi.chat.entity.ChatMessage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDTO {
    private Long roomId; // 메시지를 보낼 채팅방 ID
    private Long senderId; // 보낸 사람 ID
    private String senderNickname; // 보낸 사람 닉네임
    private String content; // 메시지 내용
    private LocalDateTime sentAt; // 보낸 시간

    // Entity -> DTO 변환 (서버가 클라이언트에게 메시지를 보낼 때 사용)
    public static ChatMessageDTO fromEntity(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setRoomId(entity.getChatRoom().getId());
        dto.setSenderId(entity.getSender().getId());
        dto.setSenderNickname(entity.getSender().getNickname());
        dto.setContent(entity.getContent());
        dto.setSentAt(entity.getCreatedAt()); // BaseEntity의 createdAt 사용
        return dto;
    }
}
