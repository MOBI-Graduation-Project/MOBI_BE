package com.mobi.mobi.friend.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.friend.dto.FriendResponseDTO;
import com.mobi.mobi.friend.dto.FriendshipResponseDTO;
import com.mobi.mobi.friend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friend API", description = "친구 관계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
@SecurityRequirement(name = "bearerAuth")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    @Operation(summary = "친구 요청 보내기 API", description = "다른 사용자에게 친구 요청을 보냅니다.")
    public ApiResponse<FriendResponseDTO> sendFriendRequest(
            @AuthenticationPrincipal String fromMemberId, @RequestParam Long toMemberId) {
        FriendResponseDTO response = friendService.sendFriendRequest(Long.parseLong(fromMemberId), toMemberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @PostMapping("/accept")
    @Operation(summary = "친구 요청 수락 API", description = "받은 친구 요청을 수락합니다.")
    public ApiResponse<FriendResponseDTO> acceptFriendRequest(
            @AuthenticationPrincipal String toMemberId, @RequestParam Long fromMemberId) {
        FriendResponseDTO response = friendService.acceptFriendRequest(fromMemberId, Long.parseLong(toMemberId));
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @PostMapping("/refuse")
    @Operation(summary = "친구 요청 거절 API", description = "받은 친구 요청을 거절합니다.")
    public ApiResponse<FriendResponseDTO> declineFriendRequest(
            @AuthenticationPrincipal String toMemberId, @RequestParam Long fromMemberId) {
        FriendResponseDTO response = friendService.declineFriendRequest(fromMemberId, Long.parseLong(toMemberId));
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @GetMapping("")
    @Operation(summary = "친구 목록 및 받은 요청 목록 조회 API", description = "나의 친구 목록과 내가 받은 친구 요청 목록을 함께 조회합니다.")
    public ApiResponse<FriendshipResponseDTO> getFriendships(@AuthenticationPrincipal String memberId) {
        FriendshipResponseDTO response = friendService.getFriendships(Long.parseLong(memberId));
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "친구 삭제 API", description = "로그인된 사용자가 친구를 삭제합니다.")
    public ApiResponse<Object> deleteFriend(
            @AuthenticationPrincipal String currentMemberId,
            @PathVariable Long friendId
    ) {
        friendService.deleteFriend(Long.parseLong(currentMemberId), friendId);
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}