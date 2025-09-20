package com.mobi.mobi.member.service;

import com.mobi.mobi.apiPayload.handler.MemberHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.member.dto.MemberProfileResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
import com.mobi.mobi.member.entity.enums.RelationStatus;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    public NicknameCheckResponseDTO checkNicknameDuplication(String nickname) {
        boolean isDuplicated = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponseDTO(nickname, isDuplicated);
    }

    public MemberProfileResponseDTO getProfile(Long viewerId, Long profileId) {
        // 프로필의 주인 사용자 정보를 조회합니다.
        Member profileOwner = memberRepository.findById(profileId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        RelationStatus status;
        if (viewerId.equals(profileId)) {
            // 1. 내 프로필을 조회하는 경우
            status = RelationStatus.SELF;
        } else {
            // 2. 다른 사람 프로필을 조회하는 경우
            Member viewer = memberRepository.findById(viewerId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)); // 로그인한 사용자 정보 조회

            // 양방향으로 친구 관계인지 확인
            boolean isFriend = friendRepository.existsByFromMemberAndToMemberAndStatus(viewer, profileOwner, FriendStatus.ACCEPTED) ||
                    friendRepository.existsByFromMemberAndToMemberAndStatus(profileOwner, viewer, FriendStatus.ACCEPTED);

            if (isFriend) {
                status = RelationStatus.FRIEND;
            } else {
                status = RelationStatus.STRANGER;
            }
        }

        return MemberProfileResponseDTO.of(profileOwner, status);
    }

    @Transactional
    public MemberProfileResponseDTO updateProfileDescribe(Long memberId, String describe) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setProfileDescribe(describe);
        return MemberProfileResponseDTO.of(member, RelationStatus.SELF);
    }

    @Transactional
    public MemberProfileResponseDTO updateAvatar(Long memberId, Avatar avatar) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setAvatar(avatar);
        return MemberProfileResponseDTO.of(member, RelationStatus.SELF);
    }
}
