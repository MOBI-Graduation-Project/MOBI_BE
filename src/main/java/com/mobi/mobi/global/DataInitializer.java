package com.mobi.mobi.global;

import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // ✨ StandardCharsets 임포트
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final StockDataRepository stockDataRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (stockDataRepository.count() > 0) {
            System.out.println("주식 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        List<StockData> stockDataList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("data_4310_20251003.csv");

        // ▼▼▼ [수정] 파일 인코딩 방식을 다시 "UTF-8"로 변경 ▼▼▼
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        reader.readLine(); // 헤더 건너뛰기

        String line;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(",");

            String code = columns[1].replace("\"", "");
            String name = columns[2].replace("\"", "");
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
        reader.close();

        stockDataRepository.saveAll(stockDataList);
        System.out.println("새로운 주식 데이터 초기화 완료. 총 " + stockDataList.size() + "개의 데이터가 저장되었습니다.");
    }
}