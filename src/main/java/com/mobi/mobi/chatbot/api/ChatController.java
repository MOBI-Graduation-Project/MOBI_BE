package com.mobi.mobi.chatbot.api;

import com.mobi.mobi.chatbot.domain.openai.service.ChatService;
import com.mobi.mobi.chatbot.domain.openai.service.OpenAIService;
import com.mobi.mobi.chatbot.dto.ChatResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final OpenAIService openAIService;
    private final ChatService chatService;

    public ChatController(OpenAIService openAIService, ChatService chatService) {
        this.openAIService = openAIService;
        this.chatService = chatService;
    }

    @ResponseBody
    @PostMapping("/chat")
    public String chat(@AuthenticationPrincipal User user, @RequestBody Map<String, String> body) {
        Long memberId = Long.parseLong(user.getUsername());
        String question = body.get("text");

        // ✅ 수정된 부분: memberId와 question을 모두 전달합니다.
        return openAIService.generate(memberId, question);
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@AuthenticationPrincipal User user, @RequestBody Map<String, String> body) {
        Long memberId = Long.parseLong(user.getUsername());
        String question = body.get("text");

        return openAIService.generateStream(memberId, question);
    }

    @ResponseBody
    @GetMapping("/chat/history")
    public List<ChatResponseDto> getChatHistory(@AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        return chatService.readAllChats(String.valueOf(memberId));
    }
}