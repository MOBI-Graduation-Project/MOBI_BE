package com.mobi.mobi.global; // 공용 패키지

import com.mobi.mobi.stockdata.entity.StockData;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.mobi.mobi.stockdata.repository.StockDataRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StockDataRepository stockDataRepository;

    @Override
    public void run(String... args) throws Exception {
        // DB에 데이터가 없으면 초기화 진행
        if (stockDataRepository.count() == 0) {
            List<StockData> stockList = new ArrayList<>();
            ClassPathResource resource = new ClassPathResource("stocks.csv");

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                // CSV 첫 줄(헤더)은 건너뛰기
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    stockList.add(StockData.builder()
                            .code(line[0])     // 1열: 종목코드
                            .name(line[1])     // 2열: 종목명
                            .market(line[2])   // 3열: 시장구분
                            .build());
                }
            }
            stockDataRepository.saveAll(stockList);
            System.out.println(stockList.size() + "개의 주식 정보를 DB에 저장했습니다.");
        }
    }}
