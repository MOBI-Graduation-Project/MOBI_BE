// SajuController.java
package com.mobi.mobi.saju.controller;

import com.mobi.mobi.saju.dto.SajuRequest;
import com.mobi.mobi.saju.dto.SajuResponse;
import com.mobi.mobi.saju.service.SajuService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import com.mobi.mobi.member.entity.Member; // ✅ Member 엔티티 import
import com.mobi.mobi.member.repository.MemberRepository; // ✅ MemberRepository import

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saju") // API 공통 주소
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // Swagger 인증 설정
public class SajuController {

    private final SajuService sajuService;
    private final MemberRepository memberRepository;

    @PostMapping("/compatibility")
    public ResponseEntity<SajuResponse> getSajuCompatibility(
            @AuthenticationPrincipal User user,
            @RequestBody SajuRequest request) {

        // 1. @AuthenticationPrincipal에서 사용자의 고유 ID (memberId)를 가져옵니다.
        Long memberId = Long.parseLong(user.getUsername());

        // 2. memberId를 사용해 데이터베이스에서 Member 정보를 조회합니다.
        //    orElseThrow를 사용해 해당 ID의 멤버가 없을 경우 예외를 발생시킵니다.
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다: " + memberId));

        // 3. 조회한 Member 객체에서 실제 사용자 이름(username)을 가져옵니다.
        String userName = findMember.getUsername();

        // 4. 가져온 실제 사용자 이름을 서비스에 전달합니다.
        String result = sajuService.getSajuCompatibility(
                userName,
                request.getBirthDate(),
                request.getStockName()
        );

        return ResponseEntity.ok(new SajuResponse(result));
    }
}