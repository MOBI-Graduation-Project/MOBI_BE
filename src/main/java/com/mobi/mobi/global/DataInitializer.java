// DataInitializer.java

package com.mobi.mobi.global;

import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import com.mobi.mobi.mydata.repository.MyDataRepository; // ✅ 1. MyDataRepository를 임포트하세요. (경로는 실제 위치에 맞게 수정)
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final MyDataRepository myDataRepository; // ✅ 2. MyDataRepository를 주입받으세요.

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("기존 데이터를 삭제하고 초기화를 시작합니다.");

        // ▼▼▼ ✨ 여기가 핵심 수정 부분입니다! ✨ ▼▼▼
        // 3. 자식 테이블 데이터를 먼저 삭제합니다.
        myDataRepository.deleteAllInBatch();

        // 4. 그 다음 부모 테이블 데이터를 삭제합니다.
        stockDataRepository.deleteAllInBatch();

        List<StockData> stockDataList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("data_4310_20251003.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            reader.readLine(); // 헤더 건너뛰기

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 7) continue;

                String code = columns[1].replace("\"", "");
                String name = columns[3].replace("\"", "");
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