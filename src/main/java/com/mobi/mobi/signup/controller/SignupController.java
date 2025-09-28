package com.mobi.mobi.signup.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.signup.dto.SignupRequestDTO;
import com.mobi.mobi.signup.dto.SignupResponseDTO;
import com.mobi.mobi.signup.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Signup API", description = "소셜 로그인 이후 추가 정보 입력 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/signup")
@SecurityRequirement(name = "bearerAuth") // Swagger UI에서 JWT 인증이 필요함을 명시
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/complete")
    @Operation(summary = "회원가입 완료 API", description = "닉네임, 설문, 약관 동의 결과를 받아 회원가입을 최종 완료합니다. (JWT 토큰 필요)")

    public ApiResponse<SignupResponseDTO> completeSignup(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody SignupRequestDTO requestDTO) {

        SignupResponseDTO responseDTO = signupService.completeSignup(Long.parseLong(memberId), requestDTO);

        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }
}