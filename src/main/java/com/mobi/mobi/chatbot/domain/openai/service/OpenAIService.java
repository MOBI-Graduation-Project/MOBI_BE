package com.mobi.mobi.chatbot.domain.openai.service;

import com.mobi.mobi.chatbot.entity.ChatEntity;
import com.mobi.mobi.chatbot.repository.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;

    // 생성자
    public OpenAIService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel, OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel, OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, ChatMemoryRepository chatMemoryRepository, ChatRepository chatRepository) {
        this.openAiChatModel = openAiChatModel;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiImageModel = openAiImageModel;
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatRepository = chatRepository;
    }

    // 1. 로그인한 사용자 ID와 질문을 파라미터로 받도록 수정
    public String generate(Long memberId, String question) {
        String userId = String.valueOf(memberId);

        // 메시지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(question);

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini") // 모델명 gpt-4.1-mini -> gpt-4o-mini 로 변경 (최신 모델)
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        // 요청 및 응답
        ChatResponse response = openAiChatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();

        // 2. 채팅 내역을 DB에 저장하는 로직 추가
        ChatEntity userChat = new ChatEntity();
        userChat.setUserId(userId);
        userChat.setType(MessageType.USER);
        userChat.setContent(question);

        ChatEntity assistantChat = new ChatEntity();
        assistantChat.setUserId(userId);
        assistantChat.setType(MessageType.ASSISTANT);
        assistantChat.setContent(answer);

        chatRepository.saveAll(List.of(userChat, assistantChat));

        return answer;
    }

    // 1. 로그인한 사용자 ID와 질문을 파라미터로 받도록 수정
    public Flux<String> generateStream(Long memberId, String question) {
        // 2. 하드코딩된 userId를 실제 memberId로 교체
        String userId = String.valueOf(memberId);

        // 전체 대화 저장용 (User 질문)
        ChatEntity chatUserEntity = new ChatEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(question);

        // 이전 대화 기억을 위한 ChatMemory 설정
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(question));

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini") // 모델명 gpt-4.1-mini -> gpt-4o-mini 로 변경 (최신 모델)
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 응답 메시지를 저장할 임시 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        // 요청 및 응답
        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> {
                    String token = response.getResult().getOutput().getText();
                    if (token != null) {
                        responseBuffer.append(token);
                        return token;
                    }
                    return null;
                })
                .doOnComplete(() -> {
                    String fullResponse = responseBuffer.toString();
                    chatMemory.add(userId, new AssistantMessage(fullResponse));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    // 전체 대화 저장용 (Assistant 답변)
                    ChatEntity chatAssistantEntity = new ChatEntity();
                    chatAssistantEntity.setUserId(userId);
                    chatAssistantEntity.setType(MessageType.ASSISTANT);
                    chatAssistantEntity.setContent(fullResponse);

                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
                });
    }

    public List<float[]> generateEmbedding(List<String> texts, String model) {
        EmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(model).build();
        EmbeddingRequest prompt = new EmbeddingRequest(texts, embeddingOptions);
        EmbeddingResponse response = openAiEmbeddingModel.call(prompt);
        return response.getResults().stream()
                .map(Embedding::getOutput)
                .toList();
    }
}