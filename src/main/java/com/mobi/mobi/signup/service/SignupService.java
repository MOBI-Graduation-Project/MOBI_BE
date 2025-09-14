package com.mobi.mobi.signup.service;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.signup.dto.SignupRequestDTO;
import com.mobi.mobi.signup.dto.SignupResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private final MemberRepository memberRepository;

    public SignupResponseDTO completeSignup(Long memberId, SignupRequestDTO requestDTO) {
        // 1. JWT 토큰에서 추출한 memberId로 회원을 찾습니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        // 2. 전달받은 정보로 Member 엔티티의 필드를 업데이트합니다.
        member.setNickname(requestDTO.getNickname());
        member.setInvestmentAnswers(requestDTO.getInvestmentAnswers());
        member.setIsPrivacyAgreed(requestDTO.getIsPrivacyAgreed());

        // 3. @Transactional 어노테이션에 의해 메서드가 종료될 때 변경된 내용이 DB에 자동으로 저장됩니다 (더티 체킹).

        // 4. 업데이트된 회원 정보를 DTO로 변환하여 반환합니다.
        return new SignupResponseDTO(member);
    }
}
