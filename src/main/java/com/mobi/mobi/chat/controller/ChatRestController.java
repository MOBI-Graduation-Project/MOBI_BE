package com.mobi.mobi.chat.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "채팅 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@SecurityRequirement(name = "bearerAuth")
public class ChatRestController {

    //채팅방 생성, 과거 내역 조회

    private final ChatService chatService;

    @PostMapping("/room")
    @Operation(summary = "채팅방 생성 또는 조회 API", description = "두 사용자 간의 1:1 채팅방 ID를 조회하거나, 없으면 새로 생성합니다.")
    public ApiResponse<Long> getOrCreateRoom(
            @AuthenticationPrincipal User user,
            @RequestParam Long otherMemberId) {
        Long myId = Long.parseLong(user.getUsername());
        Long roomId = chatService.getOrCreateRoom(myId, otherMemberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, roomId);
    }

    @GetMapping("/room/{roomId}/history")
    @Operation(summary = "채팅 내역 조회 API", description = "특정 채팅방의 이전 대화 내역을 모두 조회합니다.")
    public ApiResponse<List<ChatMessageDTO>> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDTO> history = chatService.getChatHistory(roomId);
        return ApiResponse.onSuccess(SuccessStatus._OK, history);
    }
}
