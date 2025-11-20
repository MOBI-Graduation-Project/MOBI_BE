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

        // 1. 종목 확인
        StockData stock = stockDataRepository.findByNameContaining(stockName)
                .stream()
                .filter(s -> s.getName().equalsIgnoreCase(stockName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다: " + stockName));

        // 2. 상장일 확인
        LocalDate listingDate = stock.getListingDate();
        if (listingDate == null) {
            throw new IllegalArgumentException("'" + stockName + "' 종목의 상장일 정보가 없습니다.");
        }

        // 3. 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedUserBirthDate = userBirthDate.format(formatter);
        String formattedListingDate = listingDate.format(formatter);

        // 4. 시스템 프롬프트
        String systemPrompt = "당신은 주식과 사주 명리학의 관계를 분석하여 사람과 주식 간의 궁합을 봐주는 운세 전문가입니다. " +
                "분석 결과를 친절하고 상세하게 설명해주세요. " +
                "단, 답변에는 절대로 이모티콘이나 줄바꿈 문자를 사용하지 말고, 전체 내용을 하나의 문단으로 된 줄글로 작성해야 합니다.";
        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        // 5. 사용자 프롬프트 (닉네임 제거됨)
        String userPrompt = String.format(
                "저의 생년월일은 %s입니다. 제가 궁금한 주식 종목은 '%s'이고, 이 종목의 상장일은 %s입니다. 저와 이 주식의 사주 궁합을 분석해주세요.",
                formattedUserBirthDate, stockName, formattedListingDate
        );
        UserMessage userMessage = new UserMessage(userPrompt);

        // 6. 옵션 설정
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        // 7. 호출
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);
        ChatResponse response = openAiChatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }
}