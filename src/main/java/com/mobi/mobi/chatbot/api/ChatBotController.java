package com.mobi.mobi.chatbot.api;

import com.mobi.mobi.chatbot.domain.openai.service.ChatBotService;
import com.mobi.mobi.chatbot.domain.openai.service.OpenAIService;
import com.mobi.mobi.chatbot.dto.ChatBotRequestDto;
import com.mobi.mobi.chatbot.dto.ChatBotResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// --- WebFlux 관련 import 모두 삭제 ---
// import org.springframework.security.core.context.ReactiveSecurityContextHolder;
// import org.springframework.security.core.context.SecurityContext;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;
// import org.springframework.http.MediaType;
// ------------------------------------
import org.springframework.security.core.Authentication; // (generateSimple엔 필요 없음)
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Tag(name = "chatbot-controller", description = "챗봇 관련 API")
public class ChatBotController {

    private final ChatBotService chatService;
    private final OpenAIService openAIService;

    public ChatBotController(ChatBotService chatService, OpenAIService openAIService) {
        this.chatService = chatService;
        this.openAIService = openAIService;
    }


    /**
     * [수정됨] 챗봇에게 메시지 전송 (단순 응답, 저장X)
     * - Flux 대신 String을 반환하는 순수 MVC 메소드로 변경
     * - 인증/저장 없는 'generateSimple' 호출
     */
    @ResponseBody
    @PostMapping("/chatbot") // <-- URL은 /chatbot 그대로 사용
    @Operation(summary = "챗봇에게 메시지 전송 (단순 응답, 저장X)")
    public String simpleChat(
            @RequestBody ChatBotRequestDto request
    ) {
        // OpenAIService의 새 메소드(generateSimple) 호출
        return openAIService.generateSimple(request.getContent());
    }


    /**
     * [유지] 최근 챗봇 대화 내역 조회 (MVC)
     * - 이 API는 인증이 필요하며, SecurityConfig에서 처리합니다.
     */
    @ResponseBody
    @GetMapping("/chatbot/history")
    @Operation(summary = "최근 챗봇 대화 내역 조회")
    @ApiResponses(value = {
            // ... (Swagger 응답 생략)
    })
    public List<ChatBotResponseDto> getChatHistory(@AuthenticationPrincipal User user) {

        String userId = (user != null)
                ? user.getUsername()
                : "0";

        return chatService.readAllChats(userId);
    }
}