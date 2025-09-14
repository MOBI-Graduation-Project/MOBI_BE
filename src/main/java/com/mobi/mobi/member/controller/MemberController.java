package com.mobi.mobi.member.controller;

import com.mobi.mobi.member.dto.MyProfileResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인 API", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    public ResponseEntity<NicknameCheckResponseDTO> checkNickname(
            @RequestParam("nickname") String nickname) {

        NicknameCheckResponseDTO responseDTO = memberService.checkNicknameDuplication(nickname);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회 API", description = "로그인한 사용자의 프로필 정보를 조회합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MyProfileResponseDTO> getMyProfile(@AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        MyProfileResponseDTO responseDTO = memberService.getMyProfile(memberId);
        return ResponseEntity.ok(responseDTO);
    }
}
