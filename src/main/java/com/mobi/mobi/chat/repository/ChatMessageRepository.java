package com.mobi.mobi.chat.repository;

import com.mobi.mobi.chat.entity.ChatMessage;
import com.mobi.mobi.chat.entity.ChatRoom;
import com.mobi.mobi.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지를 시간순으로 찾는 메서드 (JPA Naming Convention)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    @Modifying(clearAutomatically = true) // 영속성 컨텍스트를 비워 DB와 동기화
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chatRoom.id = :roomId AND cm.sender.id != :readerId AND cm.isRead = false")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("readerId") Long readerId);

    Optional<ChatMessage> findFirstByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 특정 채팅방에서 특정 사용자가 보내지 않은 메시지 중 안 읽은 메시지의 개수를 조회
    long countByChatRoomAndSenderNotAndIsReadIsFalse(ChatRoom chatRoom, Member member);
}

