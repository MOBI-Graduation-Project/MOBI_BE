package com.mobi.mobi.chatbot.repository;

import com.mobi.mobi.chatbot.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findByUserIdOrderByCreatedAtAsc(String userId);
}