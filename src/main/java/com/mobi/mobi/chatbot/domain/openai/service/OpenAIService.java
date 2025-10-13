package com.mobi.mobi.chatbot.domain.openai.service;

import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import com.mobi.mobi.chatbot.repository.ChatBotRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatBotRepository chatRepository;

    // 생성자
    public OpenAIService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel, OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel, OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, ChatMemoryRepository chatMemoryRepository, ChatBotRepository chatRepository) {
        this.openAiChatModel = openAiChatModel;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiImageModel = openAiImageModel;
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatRepository = chatRepository;
    }

    // 일반 채팅 (변경 없음)
    public String generate(Long memberId, String question) {
        String userId = String.valueOf(memberId);

        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(question);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        ChatResponse response = openAiChatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();

        ChatBotEntity userChat = new ChatBotEntity();
        userChat.setUserId(userId);
        userChat.setType(MessageType.USER);
        userChat.setContent(question);

        ChatBotEntity assistantChat = new ChatBotEntity();
        assistantChat.setUserId(userId);
        assistantChat.setType(MessageType.ASSISTANT);
        assistantChat.setContent(answer);

        chatRepository.saveAll(List.of(userChat, assistantChat));

        return answer;
    }

    // 스트림 채팅 (보안 및 블로킹 문제 해결)
    public Flux<String> generateStream(Long memberId, String question, Authentication authentication) {
        String userId = String.valueOf(memberId);

        // 1. 사용자 질문 저장을 Mono로 감싸고, 블로킹 작업을 위한 별도 스레드에서 실행하도록 예약
        ChatBotEntity chatUserEntity = new ChatBotEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(question);

        Mono<Void> saveUserChatMono = Mono.fromRunnable(() -> chatRepository.save(chatUserEntity))
                .subscribeOn(Schedulers.boundedElastic())
                .then();

        // 이전 대화 기억을 위한 ChatMemory 설정
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(question));

        // 옵션 및 프롬프트 설정
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 응답 메시지를 저장할 임시 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        // 2. 메인 스트림 로직
        Flux<String> openAiStream = openAiChatModel.stream(prompt)
                .mapNotNull(response -> {
                    String token = response.getResult().getOutput().getText();
                    if (token != null) {
                        responseBuffer.append(token);
                        return token;
                    }
                    return null;
                });

        // 3. 스트림이 모두 완료된 후 실행할 AI 답변 저장 로직을 Mono로 분리
        Mono<Void> saveAssistantChatMono = Mono.fromRunnable(() -> {
            String fullResponse = responseBuffer.toString();
            chatMemory.add(userId, new AssistantMessage(fullResponse));
            chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

            ChatBotEntity chatAssistantEntity = new ChatBotEntity();
            chatAssistantEntity.setUserId(userId);
            chatAssistantEntity.setType(MessageType.ASSISTANT);
            chatAssistantEntity.setContent(fullResponse);
            chatRepository.save(chatAssistantEntity);
        }).subscribeOn(Schedulers.boundedElastic()).then();

        // 4. 모든 Mono와 Flux를 순서대로 실행하도록 조합
        return Flux.defer(() ->
                        saveUserChatMono // 먼저 사용자 질문을 저장하고
                                .thenMany(openAiStream) // 그 다음에 OpenAI 스트림을 시작하고
                                .concatWith(saveAssistantChatMono.then(Mono.empty())) // 스트림이 끝나면 AI 답변을 저장
                )
                // 마지막으로 전체 스트림 파이프라인에 Security Context 전파
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    // 임베딩 (변경 없음)
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