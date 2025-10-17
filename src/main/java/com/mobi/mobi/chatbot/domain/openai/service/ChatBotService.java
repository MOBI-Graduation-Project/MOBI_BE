package com.mobi.mobi.chatbot.domain.openai.service;

import com.mobi.mobi.chatbot.dto.ChatBotResponseDto;
import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import com.mobi.mobi.chatbot.repository.ChatBotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    private final ChatBotRepository chatRepository;

    public ChatBotService(ChatBotRepository chatRepository) {
        this.chatRepository = chatRepository;
    }


    @Transactional(readOnly = true)
    public List<ChatBotResponseDto> readAllChats(String userId) {


        List<ChatBotEntity> chatEntities = chatRepository.findByUserIdOrderByCreatedAtAsc(userId);


        return chatEntities.stream()
                .map(ChatBotResponseDto::new)
                .collect(Collectors.toList());
    }
}