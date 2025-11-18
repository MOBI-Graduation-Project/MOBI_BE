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

        SystemMessage systemMessage = new SystemMessage("You are a helpful assistant.");
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