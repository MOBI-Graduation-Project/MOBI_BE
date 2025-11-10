package com.mobi.mobi.chatbot.dto;

import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import lombok.Getter;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

@Getter
public class ChatBotResponseDto {

    private final String content;
    private final boolean isBot;
    private final LocalDateTime sentAt;

    public ChatBotResponseDto(ChatBotEntity entity) {
        this.content = entity.getContent();

        this.isBot = entity.getType() == MessageType.ASSISTANT;
        this.sentAt = entity.getCreatedAt();
    }
}