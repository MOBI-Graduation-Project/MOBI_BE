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

    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member toMember);

    // 내가 보낸 친구 요청 중 '수락된' 목록 조회 (친구 목록용)
    List<Friend> findByFromMemberAndStatus(Member fromMember, FriendStatus status);

    // 내가 포함된 모든 친구 관계에서 상대방 Member 목록을 직접 조회 (친구 목록용)
    @Query("SELECT CASE WHEN f.fromMember = :member THEN f.toMember ELSE f.fromMember END " +
            "FROM Friend f WHERE (f.fromMember = :member OR f.toMember = :member) AND f.status = 'ACCEPTED'")
    List<Member> findFriendsByMember(@Param("member") Member member);

    // 나에게 온 친구 요청 목록에서 요청자 Member 목록을 직접 조회 (받은 요청 목록용)
    @Query("SELECT f.fromMember FROM Friend f WHERE f.toMember = :member AND f.status = 'PENDING'")
    List<Member> findRequestersByToMember(@Param("member") Member member);
}
