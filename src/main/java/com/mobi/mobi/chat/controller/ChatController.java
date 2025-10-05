package com.mobi.mobi.chat.controller;

import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {

        // ✨ 해결책: 세션 속성에서 직접 Principal(Authentication) 객체를 가져옵니다.
        Authentication authentication = (Authentication) headerAccessor.getSessionAttributes().get("userPrincipal");

        if (authentication == null) {
            log.error("Principal not found in session attributes. Headers: {}", headerAccessor.getMessageHeaders());
            return;
        }

        Principal principal = authentication;
        String memberId = principal.getName();
        message.setSenderId(Long.parseLong(memberId));

        log.info(">>>> WebSocket Message Received from memberId {}: {}", memberId, message.getContent());

        ChatMessageDTO savedMessageDTO = chatService.saveMessageAndGetDTO(message);

        messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessageDTO.getRoomId(), savedMessageDTO);
    }


    @MessageMapping("/chat/read")
    public void readMessage(@Payload Map<String, Long> payload, SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getSessionAttributes().get("userPrincipal");

        if (authentication == null) {
            log.error("Principal not found in session attributes during read. Headers: {}", headerAccessor.getMessageHeaders());
            return;
        }

        Long roomId = payload.get("roomId");
        Long readerId = Long.parseLong(authentication.getName());

        chatService.markMessagesAsRead(roomId, readerId);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                Map.of("type", "MESSAGES_READ", "roomId", roomId, "readerId", readerId)
        );
    }
}
