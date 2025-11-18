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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder; // <-- 11/18 추가
import org.springframework.security.core.context.SecurityContext; // <-- 11/18 추가
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono; // <-- 11/18 추가
import org.springframework.http.MediaType;

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


    @ResponseBody
    @PostMapping(value = "/chatbot", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "챗봇에게 메시지 전송 (스트리밍 응답)")
    public Flux<String> streamChat(
            @RequestBody ChatBotRequestDto request // <-- 파라미터에서 @AuthenticationPrincipal, Authentication 제거
    ) {
        // 1. WebFlux(Reactive) 인증 컨텍스트에서 Authentication 객체를 비동기(Mono)로
        Mono<Authentication> authMono = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);

        // 2. 인증 정보를 성공적으로 가져왔을 때의 로직 (flatMapMany 사용)
        return authMono.flatMapMany(authentication -> {
                    // (인증된 사용자)
                    // 가져온 authentication 객체에서 User(principal)와 memberId를 추출
                    User user = (User) authentication.getPrincipal();
                    Long memberId = Long.parseLong(user.getUsername());

                    // OpenAIService에 memberId와 authentication 객체를 전달
                    return openAIService.generateStream(memberId, request.getContent(), authentication);
                })
                // 3. 인증 정보를 가져오지 못했을 때(비로그인)의 로직 (switchIfEmpty 사용)
                .switchIfEmpty(Flux.defer(() -> {
                    // 기존 로직처럼 memberId = 0L, authentication = null 로 처리
                    Long memberId = 0L;
                    return openAIService.generateStream(memberId, request.getContent(), null);
                }));
    }


    @ResponseBody
    @GetMapping("/chatbot/history")
    @Operation(summary = "최근 챗봇 대화 내역 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "대화 내역 리스트",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ChatBotResponseDto.class)),
                            examples = @ExampleObject(
                                    value = "[\n" +
                                            "  {\n" +
                                            "    \"content\": \"안녕?\",\n" +
                                            "    \"isBot\": false,\n" +
                                            "    \"sentAt\": \"2025-09-25T00:50:11\"\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"content\": \"안녕하세요! 무엇을 도와드릴까요?\",\n" +
                                            "    \"isBot\": true,\n" +
                                            "    \"sentAt\": \"2025-09-25T00:50:12\"\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"content\": \"오늘 날씨 어때?\",\n" +
                                            "    \"isBot\": false,\n" +
                                            "    \"sentAt\": \"2025-09-25T00:50:45\"\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"content\": \"오늘 서울의 날씨는 맑습니다.\",\n" +
                                            "    \"isBot\": true,\n" +
                                            "    \"sentAt\": \"2025-09-25T00:50:46\"\n" +
                                            "  }\n" +
                                            "]"
                            )
                    )
            )
    })
    public List<ChatBotResponseDto> getChatHistory(@AuthenticationPrincipal User user) {

        String userId = (user != null)
                ? user.getUsername()
                : "0";

        return chatService.readAllChats(userId);
    }
}