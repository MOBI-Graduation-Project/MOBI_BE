package com.mobi.mobi.saju.service;

import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SajuService {

    private final StockDataRepository stockDataRepository;
    private final OpenAiChatModel openAiChatModel;

    @Transactional(readOnly = true)
    public String getSajuCompatibility(LocalDate userBirthDate, String stockName) {

        // 1. 종목 검색 (포함 검색 → 첫 번째 결과 사용)
        List<StockData> candidates = stockDataRepository.findByNameContaining(stockName);

        StockData stock = candidates.stream()
                .filter(s -> s.getName() != null)
                .findFirst()
                .orElse(null);

        // 2. 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedUserBirthDate = userBirthDate.format(formatter);

        String listingDateStr;
        if (stock != null && stock.getListingDate() != null) {
            listingDateStr = stock.getListingDate().format(formatter);
        } else {
            // 종목이 없거나 상장일이 NULL인 경우에도 예외 던지지 않고 안내 문구로 대체
            listingDateStr = "상장일 정보를 찾을 수 없습니다";
        }

        // 3. 시스템 프롬프트
        String systemPrompt =
                "당신은 주식과 사주 명리학의 관계를 분석하여 사람과 주식 간의 궁합을 봐주는 운세 전문가입니다. " +
                        "사용자의 태어난 날과 주식의 상장일을 바탕으로 투자 성향, 이 종목과의 궁합, 유의해야 할 점 등을 설명해 주세요. " +
                        "답변은 초보 투자자도 이해할 수 있게 쉽게 설명하되, 투자 심리와 운세적인 관점을 적절히 섞어 주세요. " +
                        "단, 답변에는 절대로 이모티콘이나 줄바꿈 문자를 사용하지 말고, 전체 내용을 하나의 문단으로 된 줄글로 작성해야 합니다.";

        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        // 4. 사용자 프롬프트
        String userPrompt = String.format(
                "저의 생년월일은 %s입니다. 제가 궁금한 주식 종목은 '%s'이고, 이 종목의 상장일(또는 상장 관련 정보)은 %s입니다. " +
                        "저와 이 주식의 사주 궁합을 분석해 주세요. 투자 성향, 이 종목과 잘 맞는지 여부, 주의해야 할 점, " +
                        "그리고 어떤 마음가짐으로 이 종목을 바라보면 좋을지까지 함께 설명해 주세요.",
                formattedUserBirthDate,
                stockName,
                listingDateStr
        );
        UserMessage userMessage = new UserMessage(userPrompt);

        // 5. OpenAI 옵션 설정
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        // 6. 동기 호출 (stream/block 대신 call 한 번으로 처리)
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);
        ChatResponse response = openAiChatModel.call(prompt);

        // 7. 최종 텍스트만 추출해서 반환
        return response.getResult().getOutput().getText();
    }
}
