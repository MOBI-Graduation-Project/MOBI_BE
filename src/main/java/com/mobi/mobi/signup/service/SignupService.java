package com.mobi.mobi.signup.service;

import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
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

        // 아바타/프로필
        Avatar determinedAvatar = determineAvatar(requestDTO.getInvestmentAnswers());
        member.setAvatar(determinedAvatar);

        //isSignedUp 필드를 true로 설정
        member.completeSignup();

        return new SignupResponseDTO(member);
    }


    //아바타결정
    private Avatar determineAvatar(String answers) {
        switch (answers) {
            case "111": return Avatar.AVATAR_TYPE_1;
            case "112": return Avatar.AVATAR_TYPE_2;
            case "121": return Avatar.AVATAR_TYPE_3;
            case "211": return Avatar.AVATAR_TYPE_4;
            case "122": return Avatar.AVATAR_TYPE_5;
            case "212": return Avatar.AVATAR_TYPE_6;
            case "221": return Avatar.AVATAR_TYPE_7;
            case "222": return Avatar.AVATAR_TYPE_8;
            default:
                // 기본값 또는 예외 처리
                throw new IllegalArgumentException("유효하지 않은 설문 결과 코드입니다: " + answers);
        }
    }
}
