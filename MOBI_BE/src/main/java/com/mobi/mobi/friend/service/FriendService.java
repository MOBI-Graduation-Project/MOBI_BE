package com.mobi.mobi.friend.service;

import com.mobi.mobi.apiPayload.handler.FriendHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    public FriendResponseDTO sendFriendRequest(Long fromMemberId, Long toMemberId) {
        if (fromMemberId.equals(toMemberId)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }
        Member fromMember = findMemberById(fromMemberId);
        Member toMember = findMemberById(toMemberId);

        Optional<Friend> forwardRequest = friendRepository.findByFromMemberAndToMember(fromMember, toMember);
        Optional<Friend> backwardRequest = friendRepository.findByFromMemberAndToMember(toMember, fromMember);

        if (forwardRequest.isPresent() || backwardRequest.isPresent()) {
            throw new FriendHandler(ErrorStatus.FRIEND_REQUEST_ALREADY_SENT);
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
        friendRequest.acceptRequest(); // 상태를 ACCEPTED로 변경

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
        friendRepository.delete(friendRequest); // 거절 시에는 요청 데이터를 DB에서 삭제
        return new FriendResponseDTO(friendRequest);
    }

    @Transactional(readOnly = true)
    public FriendshipResponseDTO getFriendships(Long memberId) {
        Member me = findMemberById(memberId);

        // 1. 나의 친구 목록 조회 (내가 보낸 요청이 수락된 경우만 찾으면 됨)
        List<Friend> acceptedFriendships = friendRepository.findByFromMemberAndStatus(me, FriendStatus.ACCEPTED);
        List<Member> friends = acceptedFriendships.stream()
                .map(Friend::getToMember)
                .collect(Collectors.toList());

        // 2. 내가 받은 친구 요청 목록 조회
        List<Member> requesters = friendRepository.findRequestersByToMember(me);

        return new FriendshipResponseDTO(friends, requesters);
    }

    public void deleteFriend(Long memberId, Long friendIdToDelete) {
        Member me = findMemberById(memberId);
        Member friend = findMemberById(friendIdToDelete);

        // 양방향 관계 모두 찾음
        Optional<Friend> friendship1 = friendRepository.findByFromMemberAndToMember(me, friend);
        Optional<Friend> friendship2 = friendRepository.findByFromMemberAndToMember(friend, me);

        if (friendship1.isEmpty() && friendship2.isEmpty()) {
            throw new FriendHandler(ErrorStatus.NOT_FRIEND);
        }

        // 존재하는 관계만 삭제
        friendship1.ifPresent(friendRepository::delete);
        friendship2.ifPresent(friendRepository::delete);
    }

    private Friend findPendingRequest(Member fromMember, Member toMember) {
        return friendRepository.findByFromMemberAndToMember(fromMember, toMember)
                .filter(friend -> friend.getStatus() == FriendStatus.PENDING)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
