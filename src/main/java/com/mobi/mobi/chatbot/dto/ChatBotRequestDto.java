package com.mobi.mobi.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.ai.chat.messages.MessageType;

@Setter
@Getter
@NoArgsConstructor
@Schema(name = "ChatBotRequestDto", description = "사용자가 챗봇으로 보내는 메시지")
public class ChatBotRequestDto {

    @Schema(description = "유저 ID", example = "user123")
    private String userId;

    @Schema(description = "메시지 내용", example = "주가 예측해줘")
    private String content;

    @Schema(description = "메시지 타입(USER, ASSISTANT...)", example = "USER")
    private MessageType type;
}
