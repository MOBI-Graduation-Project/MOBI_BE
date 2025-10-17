package com.mobi.mobi.global;

import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public void run(ApplicationArguments args) throws IOException, CsvValidationException {
        if (stockDataRepository.count() > 0) {
            System.out.println("주식 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        List<StockData> stockDataList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("data_4310_20251003.csv");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1)
                .build()) {


            String[] line;
            while ((line = reader.readNext()) != null) {

                String code = line[1];        // 단축코드
                String name = line[3];        // 한글 종목약명

                // 인덱스수정 5->6
                int listingDateIndex = 6;

                LocalDate listingDate = null;
                try {
                    // line.length 체크도 5에서 6으로 수정
                    if (line.length > listingDateIndex && !line[listingDateIndex].isEmpty()) {
                        listingDate = LocalDate.parse(line[listingDateIndex], formatter); // 상장일
                    }
                } catch (Exception e) {
                    System.out.println("날짜 파싱 오류 (종목코드: " + code + "): 잘못 읽힌 값: " + line[listingDateIndex]);
                }

                // 인덱스 6->7
                String market = line[7];      // 시장구분

                stockDataList.add(StockData.builder()
                        .code(code)
                        .name(name)
                        .market(market)
                        .listingDate(listingDate)
                        .build());
            }
        }

        stockDataRepository.saveAll(stockDataList);
        System.out.println("주식 데이터 초기화 완료. 총 " + stockDataList.size() + "개의 데이터가 저장되었습니다.");
    }
}