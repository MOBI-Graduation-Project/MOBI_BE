package com.mobi.mobi.member.service;

import com.mobi.mobi.apiPayload.handler.MemberHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.member.dto.MyProfileResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
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

    public MyProfileResponseDTO getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return new MyProfileResponseDTO(member);
    }

    @Transactional
    public MyProfileResponseDTO updateProfileDescribe(Long memberId, String describe) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setProfileDescribe(describe);
        return new MyProfileResponseDTO(member);
    }

    @Transactional
    public MyProfileResponseDTO updateAvatar(Long memberId, Avatar avatar) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setAvatar(avatar);
        return new MyProfileResponseDTO(member);
    }
}
