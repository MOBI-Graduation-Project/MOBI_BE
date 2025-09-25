package com.mobi.mobi.chatbot.domain.openai.service;

import com.mobi.mobi.chatbot.dto.ChatResponseDto; // DTO 임포트
import com.mobi.mobi.chatbot.entity.ChatEntity;
import com.mobi.mobi.chatbot.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 스프링의 Transactional 사용
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // 반환 타입을 List<ChatResponseDto>로 변경
    @Transactional(readOnly = true)
    public List<ChatResponseDto> readAllChats(String userId) {

        // 1. DB에서 원본 데이터(Entity) 목록을 가져옵니다.
        List<ChatEntity> chatEntities = chatRepository.findByUserIdOrderByCreatedAtAsc(userId);

        // 2. 가져온 엔티티 목록을 DTO 목록으로 변환합니다.
        return chatEntities.stream()                 // 리스트를 스트림으로 변환
                .map(ChatResponseDto::new)           // 각 엔티티를 ChatResponseDto 객체로 생성
                .collect(Collectors.toList());       // 결과를 다시 리스트로 수집
    }
}