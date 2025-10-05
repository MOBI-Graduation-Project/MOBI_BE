// DataInitializer.java

package com.mobi.mobi.global;

import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // ✅ Transactional 추가

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final StockDataRepository stockDataRepository;

    @Override
    @Transactional // ✅ 데이터 삭제 및 저장을 하나의 트랜잭션으로 묶기 위해 추가
    public void run(ApplicationArguments args) throws Exception {
        // ▼▼▼ [수정] 기존 로직 대신, 매번 데이터를 새로고침하도록 변경 ▼▼▼
        System.out.println("기존 주식 데이터를 삭제하고 초기화를 시작합니다.");
        stockDataRepository.deleteAllInBatch(); // 기존 데이터 모두 삭제

        List<StockData> stockDataList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("data_4310_20251003.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            reader.readLine(); // 헤더 건너뛰기

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length < 7) continue; // 데이터가 부족한 줄은 건너뛰기

                String code = columns[1].replace("\"", "");

                // ▼▼▼ ✨ 여기가 핵심 수정 부분입니다! ✨ ▼▼▼
                // 세 번째 열(columns[2]) 대신 네 번째 열(columns[3])을 'name'으로 사용합니다.
                String name = columns[3].replace("\"", ""); // "한글 종목약명"

                String market = columns[6].replace("\"", "");
                LocalDate listingDate = null;
                try {
                    if (columns.length > 5 && !columns[5].replace("\"", "").isEmpty()) {
                        listingDate = LocalDate.parse(columns[5].replace("\"", ""), formatter);
                    }
                } catch (Exception e) {
                    System.out.println("날짜 파싱 오류 (종목코드: " + code + "): " + columns[5]);
                }

                stockDataList.add(StockData.builder()
                        .code(code)
                        .name(name)
                        .market(market)
                        .listingDate(listingDate)
                        .build());
            }
        }

        stockDataRepository.saveAll(stockDataList);
        System.out.println("새로운 주식 데이터 초기화 완료. 총 " + stockDataList.size() + "개의 데이터가 저장되었습니다.");
    }
}