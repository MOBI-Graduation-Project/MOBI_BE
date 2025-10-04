package com.mobi.mobi.chat.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.entity.ChatRoomListDTO;
import com.mobi.mobi.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "채팅 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/room")
    @Operation(summary = "채팅방 생성 또는 조회 API", description = "두 사용자 간의 1:1 채팅방 ID를 조회하거나, 없으면 새로 생성합니다.")
    public ApiResponse<Long> getOrCreateRoom(
            @AuthenticationPrincipal User user, // String 대신 User 객체로 받도록 변경
            @RequestParam Long otherMemberId) {

        if (user == null) {
            log.error("User is null. Check JWT Filter chain.");
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.", null);
        }

        String myId = user.getUsername();
        Long roomId = chatService.getOrCreateRoom(Long.parseLong(myId), otherMemberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, roomId);
    }

    @GetMapping("/room/{roomId}/history")
    @Operation(summary = "채팅 내역 조회 API", description = "특정 채팅방의 이전 대화 내역을 모두 조회합니다.")
    public ApiResponse<List<ChatMessageDTO>> getChatHistory(@PathVariable Long roomId, @AuthenticationPrincipal User user) {
        List<ChatMessageDTO> history = chatService.getChatHistory(roomId);
        return ApiResponse.onSuccess(SuccessStatus._OK, history);
    }

    @GetMapping("/rooms")
    @Operation(summary = "채팅방 목록 조회 API", description = "사용자가 참여하고 있는 모든 채팅방의 목록을 조회합니다.")
    public ApiResponse<List<ChatRoomListDTO>> getChatRooms(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("User is null. Check JWT Filter chain.");
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.", null);
        }
        Long myId = Long.parseLong(user.getUsername());
        List<ChatRoomListDTO> chatRooms = chatService.getChatRooms(myId);
        return ApiResponse.onSuccess(SuccessStatus._OK, chatRooms);
    }
}
