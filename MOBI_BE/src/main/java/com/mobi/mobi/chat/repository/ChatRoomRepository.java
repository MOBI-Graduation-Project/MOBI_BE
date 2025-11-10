package com.mobi.mobi.chat.repository;

import com.mobi.mobi.chat.entity.ChatRoom;
import com.mobi.mobi.chat.entity.enums.ChatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 1:1 채팅방이 이미 존재하는지 확인하는 쿼리
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.chatRoomMembers crm1 JOIN cr.chatRoomMembers crm2 " +
            "WHERE cr.chatType = 'FRIEND' AND crm1.member.id = :memberId1 AND crm2.member.id = :memberId2")
    Optional<ChatRoom> findFriendChatRoomByMemberIds(@Param("memberId1") Long memberId1, @Param("memberId2") Long memberId2);

    // 특정 타입의 모든 채팅방 목록을 조회
    List<ChatRoom> findByChatType(ChatType chatType);
}