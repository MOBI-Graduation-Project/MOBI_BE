package com.mobi.mobi.chat.controller;

import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    // 클라이언트가 /pub으로 보낸 메시지를 처리

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // 클라이언트가 /pub/chat/message 로 메시지를 보내면 이 메서드가 호출된다
    @MessageMapping("/chat/message")
    public void message(ChatMessageDTO message) {

        //테스트용 로그
        log.info(">>>> WebSocket Message Received: {}", message.getContent());
        // 1. DTO를 반환하는 서비스 메서드를 호출
        ChatMessageDTO savedMessageDTO = chatService.saveMessageAndGetDTO(message);

        // 2. 해당 채팅방을 구독하고 있는 모든 클라이언트에게 DTO를 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessageDTO.getRoomId(),
                savedMessageDTO);
    }


    // 클라이언트가 /pub/chat/read 로 메시지를 보내면 이 메서드가 호출된다
    @MessageMapping("/chat/read")
    public void readMessage(@Payload Map<String, Long> payload) {
        Long roomId = payload.get("roomId");
        Long readerId = payload.get("readerId");

        // 1. DB에서 해당 채팅방의 메시지들을 읽음 처리함
        chatService.markMessagesAsRead(roomId, readerId);

        // 2. 메시지가 읽혔다는 사실을 채팅방의 모든 구독자에게 알림
        // 클라이언트는 이 메시지를 받고 UI를 업데이트 할 수 있음 (e.g., 안읽음 숫자 제거)
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                Map.of("type", "MESSAGES_READ", "roomId", roomId, "readerId", readerId)
        );
    }
}
