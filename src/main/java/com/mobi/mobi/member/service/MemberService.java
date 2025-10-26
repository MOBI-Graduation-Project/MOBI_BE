package com.mobi.mobi.member.service;

import com.mobi.mobi.apiPayload.handler.FriendHandler;
import com.mobi.mobi.apiPayload.handler.MemberHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.common.service.S3Service;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.member.dto.MemberProfileResponseDTO;
import com.mobi.mobi.member.dto.MemberSearchResponseDTO;
import com.mobi.mobi.member.dto.NicknameCheckResponseDTO;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.Avatar;
import com.mobi.mobi.member.entity.enums.RelationStatus;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final S3Service s3Service;

    public NicknameCheckResponseDTO checkNicknameDuplication(String nickname) {
        boolean isDuplicated = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponseDTO(nickname, isDuplicated);
    }

    public MemberProfileResponseDTO getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        // 내 프로필이므로 관계는 항상 SELF 입니다.
        return MemberProfileResponseDTO.of(member, RelationStatus.SELF);
    }

    public MemberProfileResponseDTO getProfile(Long viewerId, Long profileId) {
        // 내 프로필을 조회하는 경우, 새로 만든 API 사용 유도
        if (viewerId.equals(profileId)) {
            return getMyProfile(viewerId);
        }

        // 다른 사용자 프로필 조회 로직
        Member profileOwner = memberRepository.findById(profileId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member viewer = memberRepository.findById(viewerId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        boolean isFriend = friendRepository.existsByFromMemberAndToMemberAndStatus(viewer, profileOwner, FriendStatus.ACCEPTED) ||
                friendRepository.existsByFromMemberAndToMemberAndStatus(profileOwner, viewer, FriendStatus.ACCEPTED);

        RelationStatus status = isFriend ? RelationStatus.FRIEND : RelationStatus.STRANGER;

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

    public List<MemberSearchResponseDTO.MemberInfo> searchMembersByNickname(Long viewerId, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return new ArrayList<>(); // 검색어가 없으면 빈 목록 반환
        }

        Member viewer = memberRepository.findById(viewerId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Member> foundMembers = memberRepository.findByNicknameContainingIgnoreCase(nickname);

        return foundMembers.stream().map(member -> {
            RelationStatus status;
            if (viewer.getId().equals(member.getId())) {
                status = RelationStatus.SELF;
            } else {
                // 친구 관계인지 확인 (양방향)
                boolean isFriend = friendRepository.existsByFromMemberAndToMemberAndStatus(viewer, member, FriendStatus.ACCEPTED) ||
                        friendRepository.existsByFromMemberAndToMemberAndStatus(member, viewer, FriendStatus.ACCEPTED);
                status = isFriend ? RelationStatus.FRIEND : RelationStatus.STRANGER;
            }
            return MemberSearchResponseDTO.of(member, status);
        }).collect(Collectors.toList());
    }

    @Transactional
    public MemberProfileResponseDTO updateProfileImage(Long memberId, MultipartFile imageFile) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // S3에 이미지 업로드하고 URL을 받아옵니다.
        String imageUrl = s3Service.uploadFile(imageFile);

        // 멤버 엔티티의 프로필 이미지 URL을 업데이트합니다.
        member.setProfileImgUrl(imageUrl);

        // 변경된 멤버 정보로 DTO를 생성하여 반환합니다.
        // @Transactional에 의해 메서드 종료 시 변경된 내용이 DB에 반영됩니다.
        return MemberProfileResponseDTO.of(member, RelationStatus.SELF);
    }
}
