package com.mobi.mobi.chatbot.api;

import com.mobi.mobi.chatbot.domain.openai.service.ChatBotService;
import com.mobi.mobi.chatbot.domain.openai.service.OpenAIService;
import com.mobi.mobi.chatbot.dto.ChatBotResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;

@Controller
@SecurityRequirement(name = "bearerAuth")
public class ChatBotController {

    private final OpenAIService openAIService;
    private final ChatBotService chatService;

    public ChatBotController(OpenAIService openAIService, ChatBotService chatService) {
        this.openAIService = openAIService;
        this.chatService = chatService;
    }

    @ResponseBody
    @PostMapping("/chatbot")
    public String chat(@AuthenticationPrincipal User user, @RequestBody Map<String, String> body) {
        Long memberId = Long.parseLong(user.getUsername());
        String question = body.get("text");


        return openAIService.generate(memberId, question);
    }

    @ResponseBody
    @PostMapping("/chatbot/stream")
    public Flux<String> streamChat(@AuthenticationPrincipal User user, Authentication authentication, @RequestBody Map<String, String> body) { // Authentication 파라미터 추가
        Long memberId = Long.parseLong(user.getUsername());
        String question = body.get("text");


        return openAIService.generateStream(memberId, question, authentication);
    }

    @ResponseBody
    @GetMapping("/chatbot/history")
    public List<ChatBotResponseDto> getChatHistory(@AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        return chatService.readAllChats(String.valueOf(memberId));
    }
}