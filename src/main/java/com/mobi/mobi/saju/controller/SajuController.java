package com.mobi.mobi.saju.controller;

import com.mobi.mobi.saju.dto.SajuRequest;
import com.mobi.mobi.saju.dto.SajuResponse;
import com.mobi.mobi.saju.service.SajuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saju")
@RequiredArgsConstructor
@Tag(name = "Saju", description = "사주/주식 궁합 API")
public class SajuController {

    private final SajuService sajuService;
    // private final MemberRepository memberRepository; // [삭제] DB 조회 안 함

    @PostMapping("/compatibility")
    @Operation(summary = "주식 사주 궁합", description = "닉네임, 생년월일, 종목명을 입력받아 결과를 반환합니다.")
    // @SecurityRequirement(name = "bearerAuth") // [삭제] 자물쇠 제거 (인증 불필요)
    public ResponseEntity<SajuResponse> getSajuCompatibility(
            // @AuthenticationPrincipal User user, // [삭제] 토큰 안 받음
            @RequestBody SajuRequest request) {


        String nickname = request.getNickname();

        // 2. 서비스 호출 (서비스의 첫 번째 파라미터로 nickname 전달)

        String result = sajuService.getSajuCompatibility(
                nickname,
                request.getBirthDate(),
                request.getStockName()
        );

        return ResponseEntity.ok(new SajuResponse(result));
    }
}