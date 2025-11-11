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
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
@Tag(name = "chat-bot-controller", description = "챗봇 관련 API")
public class ChatBotController {

    private final ChatBotService chatService;
    private final OpenAIService openAIService;

    public ChatBotController(ChatBotService chatService, OpenAIService openAIService) {
        this.chatService = chatService;
        this.openAIService = openAIService;
    }

    @ResponseBody
    @PostMapping("/chatbot")
    @Operation(summary = "챗봇에게 메시지 전송 (스트리밍 응답)")
    public Flux<String> streamChat(
            @AuthenticationPrincipal User user,
            Authentication authentication,
            @RequestBody ChatBotRequestDto request
    ) {
        Long memberId = Long.parseLong(user.getUsername());
        // 요청 DTO에서 content만 뽑아서 OpenAI 서비스로 넘김
        return openAIService.generateStream(memberId, request.getContent(), authentication);
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
        Long memberId = Long.parseLong(user.getUsername());
        return chatService.readAllChats(String.valueOf(memberId));
    }
}
