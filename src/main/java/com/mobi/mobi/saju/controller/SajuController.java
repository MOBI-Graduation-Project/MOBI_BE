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

    @PostMapping("/compatibility")
    @Operation(summary = "주식 사주 궁합", description = "원하는 주식 종목과의 궁합을 봅니다.")
    public ResponseEntity<SajuResponse> getSajuCompatibility(
            @RequestBody SajuRequest request) {

        // 인증 정보(User)나 닉네임 없이, 오직 요청 데이터만 서비스로 전달
        String result = sajuService.getSajuCompatibility(
                request.getBirthDate(),
                request.getStockName()
        );

        return ResponseEntity.ok(new SajuResponse(result));
    }
}