package com.mobi.mobi.chat.controller;

import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.entity.ChatMessage;
import com.mobi.mobi.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // 클라이언트가 /pub으로 보낸 메시지를 처리

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // 클라이언트가 /pub/chat/message 로 메시지를 보내면 이 메서드가 호출됩니다.
    @MessageMapping("/chat/message")
    public void message(ChatMessageDTO message) {
        // 1. 메시지를 DB에 저장합니다.
        ChatMessage savedMessage = chatService.saveMessage(message);

        // 2. 해당 채팅방을 구독하고 있는 모든 클라이언트에게 메시지를 전송합니다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessage.getChatRoom().getId(),
                ChatMessageDTO.fromEntity(savedMessage));
    }
}
