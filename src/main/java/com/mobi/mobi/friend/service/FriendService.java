package com.mobi.mobi.friend.service;

import com.mobi.mobi.friend.dto.FriendResponseDTO;
import com.mobi.mobi.friend.dto.FriendshipResponseDTO;
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

    public FriendResponseDTO sendFriendRequest(Long fromMemberId, Long toMemberId) {
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);

        Optional<Friend> forwardRequest = friendRepository.findByFromMemberAndToMember(fromMember, toMember);
        Optional<Friend> backwardRequest = friendRepository.findByFromMemberAndToMember(toMember, fromMember);

        if (forwardRequest.isPresent() || backwardRequest.isPresent()) {
            throw new IllegalStateException("이미 친구 요청을 보냈거나 처리 대기 중인 관계입니다.");
        }

        Friend newFriendRequest = Friend.builder()
                .fromMember(fromMember).toMember(toMember).status(FriendStatus.PENDING).build();
        friendRepository.save(newFriendRequest);
        return new FriendResponseDTO(newFriendRequest);
    }

    public FriendResponseDTO acceptFriendRequest(Long fromMemberId, Long toMemberId) {
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);
        Friend friendRequest = findPendingRequest(fromMember, toMember);
        friendRequest.acceptRequest();

        friendRepository.findByFromMemberAndToMember(toMember, fromMember).orElseGet(() -> {
            Friend reciprocalFriend = Friend.builder()
                    .fromMember(toMember).toMember(fromMember).status(FriendStatus.ACCEPTED).build();
            return friendRepository.save(reciprocalFriend);
        });
        return new FriendResponseDTO(friendRequest);
    }

    public FriendResponseDTO declineFriendRequest(Long fromMemberId, Long toMemberId) {
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);
        Friend friendRequest = findPendingRequest(fromMember, toMember);
        friendRepository.delete(friendRequest);
        return new FriendResponseDTO(friendRequest);
    }

    @Transactional(readOnly = true)
    public FriendshipResponseDTO getFriendships(Long memberId) {
        Member me = findMemberById(memberId);
        List<Member> friends = friendRepository.findFriendsByMember(me);
        List<Member> requesters = friendRepository.findRequestersByToMember(me);
        return new FriendshipResponseDTO(friends, requesters);
    }

    public void deleteFriend(Long memberId, Long friendIdToDelete) {
        Member me = findMemberById(memberId);
        Member friend = findMemberById(friendIdToDelete);
        friendRepository.findByFromMemberAndToMember(me, friend).ifPresent(friendRepository::delete);
        friendRepository.findByFromMemberAndToMember(friend, me).ifPresent(friendRepository::delete);
    }

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
