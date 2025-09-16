package com.mobi.mobi.friend.service;

import com.mobi.mobi.friend.dto.FriendListResponseDTO;
import com.mobi.mobi.friend.dto.FriendRequestResponseDTO;
import com.mobi.mobi.friend.dto.FriendResponseDTO;
import com.mobi.mobi.friend.dto.FriendshipDTO;
import com.mobi.mobi.friend.entity.Friend;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.friend.repository.FriendRepository;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    // 친구 요청 보내기
    public FriendResponseDTO sendFriendRequest(Long fromMemberId, Long toMemberId) {
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);

        // A->B 또는 B->A 관계를 모두 찾아봅니다.
        Optional<Friend> existingRequest = friendRepository.findByFromMemberAndToMember(fromMember, toMember)
                .or(() -> friendRepository.findByFromMemberAndToMember(toMember, fromMember));

        if (existingRequest.isPresent()) {
            Friend friend = existingRequest.get();
            if (friend.getStatus() == FriendStatus.PENDING || friend.getStatus() == FriendStatus.ACCEPTED) {
                throw new IllegalStateException("이미 친구 요청을 보냈거나 친구 관계입니다.");
            }
            // 거절된 상태였다면, 다시 PENDING으로 변경 (재요청)
            if (friend.getStatus() == FriendStatus.DECLINED) {
                // 이 관계가 내가 보낸 요청이었는지, 상대방이 보낸 요청이었는지 확인
                if(friend.getFromMember().getId().equals(fromMemberId)) {
                    friend.setStatus(FriendStatus.PENDING);
                    return new FriendResponseDTO(friend);
                }
            }
        }

        Friend newFriendRequest = Friend.builder()
                .fromMember(fromMember).toMember(toMember).status(FriendStatus.PENDING).build();
        friendRepository.save(newFriendRequest);
        return new FriendResponseDTO(newFriendRequest);
    }

    // 친구 요청 수락
    public void acceptFriendRequest(Long fromMemberId, Long toMemberId) {
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);

        Friend friendRequest = findPendingRequest(fromMember, toMember);
        friendRequest.acceptRequest();
    }

    // 친구 요청 거절
    public void declineFriendRequest(Long fromMemberId, Long toMemberId) {
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);
        Friend friendRequest = findPendingRequest(fromMember, toMember);
        // 거절 시에는 해당 요청 데이터를 DB에서 삭제
        friendRepository.delete(friendRequest);
    }

    // 친구 목록 및 받은 요청 목록 동시 조회
    @Transactional(readOnly = true)
    public FriendshipDTO getFriendships(Long memberId) {
        Member me = findMemberById(memberId);
        // 나의 친구 목록 (양방향 조회)
        List<Friend> acceptedFriends = friendRepository.findAllAcceptedFriendsByMember(me);
        // 내가 받은 친구 요청 목록
        List<Friend> pendingRequests = friendRepository.findByToMemberAndStatus(me, FriendStatus.PENDING);
        return new FriendshipDTO(acceptedFriends, pendingRequests, memberId);
    }

    // 친구 삭제 (상호 관계 모두 삭제)
    public void deleteFriend(Long memberId, Long friendIdToDelete) {
        Member me = findMemberById(memberId);
        Member friend = findMemberById(friendIdToDelete);
        friendRepository.findByFromMemberAndToMember(me, friend).ifPresent(friendRepository::delete);
        friendRepository.findByFromMemberAndToMember(friend, me).ifPresent(friendRepository::delete);
    }

    // PENDING 상태의 친구 요청을 찾는 private 메서드
    private Friend findPendingRequest(Member fromMember, Member toMember) {
        return friendRepository.findByFromMemberAndToMember(fromMember, toMember)
                .filter(friend -> friend.getStatus() == FriendStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));
    }

}
