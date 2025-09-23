package com.mobi.mobi.member.repository;

import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일을 통해 사용자를 찾는 메서드 (소셜 로그인 사용자를 구분하기 위함)
    Optional<Member> findByEmail(String email);
    boolean existsByNickname(String nickname);
}
