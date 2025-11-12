package com.mobi.mobi.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

@Getter
@Schema(name = "ChatBotResponseDto", description = "챗봇/사용자 한 줄 대화")
public class ChatBotResponseDto {

    @Schema(description = "메시지 내용", example = "안녕?")
    private final String content;

    @JsonProperty("isBot")
    @Schema(description = "봇이 보낸 메시지 여부", example = "false")
    private final boolean isBot;

    @Schema(description = "보낸 시각", example = "2025-09-25T00:50:11")
    private final LocalDateTime sentAt;

    public ChatBotResponseDto(ChatBotEntity entity) {
        this.content = entity.getContent();
        this.isBot = entity.getType() == MessageType.ASSISTANT;
        this.sentAt = entity.getCreatedAt();
    }
}
