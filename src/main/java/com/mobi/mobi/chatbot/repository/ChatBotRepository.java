package com.mobi.mobi.chatbot.repository;

import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatBotRepository extends JpaRepository<ChatBotEntity, Long> {
    List<ChatBotEntity> findByUserIdOrderByCreatedAtAsc(String userId);

    List<ChatBotEntity> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}