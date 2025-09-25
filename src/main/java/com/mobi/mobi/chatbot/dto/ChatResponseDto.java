package com.mobi.mobi.chatbot.dto;

import com.mobi.mobi.chatbot.entity.ChatEntity;
import lombok.Getter;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

@Getter
public class ChatResponseDto {

    // 클라이언트에게 보여줄 내용, 메시지 타입, 생성 시간 필드
    private final String content;
    private final org.springframework.ai.chat.messages.MessageType type;
    private final LocalDateTime createdAt;

    // ChatEntity 객체를 ChatResponseDto 객체로 쉽게 변환하기 위한 생성자
    public ChatResponseDto(ChatEntity entity) {
        this.content = entity.getContent();
        this.type = entity.getType();
        this.createdAt = entity.getCreatedAt();
    }
}