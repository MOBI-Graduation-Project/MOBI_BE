package com.mobi.mobi.chat.repository;

import com.mobi.mobi.chat.entity.ChatRoom;
import com.mobi.mobi.chat.entity.ChatRoomMember;
import com.mobi.mobi.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    // 특정 사용자가 특정 채팅방에 참여하고 있는지 확인
    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}

