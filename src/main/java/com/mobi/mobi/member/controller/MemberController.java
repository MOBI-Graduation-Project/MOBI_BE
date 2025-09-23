package com.mobi.mobi.member.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.member.dto.MemberProfileResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.dto.UpdateAvatarRequestDTO;
import com.mobi.mobi.member.dto.UpdateDescribeRequestDTO;
import com.mobi.mobi.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인 API", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    public ApiResponse<NicknameCheckResponseDTO> checkNickname(
            @RequestParam("nickname") String nickname) {

        NicknameCheckResponseDTO responseDTO = memberService.checkNicknameDuplication(nickname);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @GetMapping("/profile/{memberId}")
    @Operation(summary = "사용자 프로필 조회 API", description = "URL에 포함된 memberId를 사용하여 특정 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> getProfile(
            @AuthenticationPrincipal String viewerId, // 수정됨
            @Parameter(description = "조회할 사용자의 ID") @PathVariable("memberId") Long profileId
    ) {
        MemberProfileResponseDTO responseDTO = memberService.getProfile(Long.parseLong(viewerId), profileId); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }


    @PatchMapping("/profile/describe")
    @Operation(summary = "내 프로필 한줄메시지 수정 API", description = "로그인한 사용자의 한줄메시지를 수정합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> updateProfileDescribe(
            @AuthenticationPrincipal String memberId, // 수정됨
            @Valid @RequestBody UpdateDescribeRequestDTO request) {
        MemberProfileResponseDTO responseDTO = memberService.updateProfileDescribe(Long.parseLong(memberId), request.getProfileDescribe()); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @PatchMapping("/profile/avatar")
    @Operation(summary = "내 프로필 아바타 수정 API", description = "로그인한 사용자의 아바타를 수정합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> updateAvatar(
            @AuthenticationPrincipal String memberId, // 수정됨
            @Valid @RequestBody UpdateAvatarRequestDTO request) {
        MemberProfileResponseDTO responseDTO = memberService.updateAvatar(Long.parseLong(memberId), request.getAvatar()); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }
}