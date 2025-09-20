package com.mobi.mobi.chat.repository;

import com.mobi.mobi.chat.entity.ChatMessage;
import com.mobi.mobi.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지를 시간순으로 찾는 메서드 (JPA Naming Convention)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}
