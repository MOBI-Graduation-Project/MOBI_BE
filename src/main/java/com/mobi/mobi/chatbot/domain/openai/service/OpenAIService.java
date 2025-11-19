package com.mobi.mobi.chatbot.domain.openai.service;

import com.mobi.mobi.chatbot.entity.ChatBotEntity;
import com.mobi.mobi.chatbot.repository.ChatBotRepository;

import lombok.Getter;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    @Getter
    private final ChatBotRepository chatRepository;


    public OpenAIService(OpenAiChatModel openAiChatModel,
                         OpenAiEmbeddingModel openAiEmbeddingModel,

                         ChatBotRepository chatRepository) {
        this.openAiChatModel = openAiChatModel;
        this.openAiEmbeddingModel = openAiEmbeddingModel;

        this.chatRepository = chatRepository;
    }

    public String generateSimple(String question) {

        SystemMessage systemMessage = new SystemMessage("당신의 금융에 관련한 전문가입니다. 제가 '실시간 금융 데이터 조회'라고 질문하면, 현시점의 대한민국의 기준금리와, 코스피, 코스닥 지수의 값을 출력합니다. 그외에 다른 질문에도 친절하게 답변합니다. 단, 답변에는 절대로 이모티콘이나 줄바꿈 문자를 사용하지 말고, 전체 내용을 하나의 문단으로 된 줄글로 작성해야 합니다.");
        UserMessage userMessage = new UserMessage(question);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);


        ChatResponse response = openAiChatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();


        return answer;
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