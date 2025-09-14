package com.mobi.mobi.member.service;

import com.mobi.mobi.member.dto.MyProfileResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public NicknameCheckResponseDTO checkNicknameDuplication(String nickname) {
        boolean isDuplicated = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponseDTO(nickname, isDuplicated);
    }

    public MyProfileResponseDTO getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        return new MyProfileResponseDTO(member);
    }
}
