package com.mobi.mobi.friend.repository;

import com.mobi.mobi.friend.entity.Friend;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // A가 B에게 보낸 요청을 찾기 위해 사용
    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member toMember);

    // 내가 포함된 모든 '수락된' 친구 관계 조회 (친구 목록용)
    @Query("SELECT f FROM Friend f WHERE (f.fromMember = :member OR f.toMember = :member) AND f.status = 'ACCEPTED'")
    List<Friend> findAllAcceptedFriendsByMember(@Param("member") Member member);

    // 나에게 온 친구 요청 중 '대기중인' 목록 조회 (받은 요청 목록용)
    List<Friend> findByToMemberAndStatus(Member toMember, FriendStatus status);
}