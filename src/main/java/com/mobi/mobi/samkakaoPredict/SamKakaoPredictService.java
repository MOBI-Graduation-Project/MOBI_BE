package com.mobi.mobi.samkakaoPredict;

import com.mobi.mobi.external.krx.KrxApiClient;
import com.mobi.mobi.external.krx.KrxStockInfo;
import com.mobi.mobi.samkakaoPredict.dto.StockInfoDTO;
import com.mobi.mobi.samkakaoPredict.dto.StockPredictMetaDTO;
import com.mobi.mobi.samkakaoPredict.dto.StockPredictResponseDTO;
import com.mobi.mobi.samkakaoPredict.dto.StockPriceRecordDTO;
import com.mobi.mobi.samkakaoPredict.entity.SamKakaoPredictPrice;
import com.mobi.mobi.samkakaoPredict.repository.SamKakaoPredictPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SamKakaoPredictService {

    private final KrxApiClient krxApiClient;
    private final SamKakaoPredictPriceRepository predictPriceRepository;


    public List<StockPredictResponseDTO> getPrediction(String stockCode) {

        // 1. 코드 정리 + 이름 매핑
        String cleanedCode = cleanCode(stockCode); // "A005930" -> "005930"
        String stockName = switch (cleanedCode) {
            case "005930" -> "삼성전자";
            case "035720" -> "카카오";
            default -> null;
        };

        if (stockName == null) {
            // 지원하지 않는 종목이면 빈 배열
            return List.of();
        }

        // 2. KRX에서 실제 3영업일 가져오기
        List<StockPriceRecordDTO> actualRecords = fetchLast3BusinessDaysFromKrx(cleanedCode);

        // 3. DB에서 예측 가져오기 (없으면 빈 리스트)
        List<SamKakaoPredictPrice> predictedEntities = fetchPredictionEntitiesFromDb(cleanedCode);

        // 3-1. 엔티티 → DTO
        List<StockPriceRecordDTO> predictedRecords = predictedEntities.stream()
                .map(p -> StockPriceRecordDTO.builder()
                        .date(p.getTargetDate().toString())
                        .predictedPrice(p.getPredictedPrice())
                        .isPredicted(true)
                        .build())
                .collect(Collectors.toList());

        // 4. meta.predictionGeneratedAt 설정

        String predictionGeneratedAt = predictedEntities.stream()
                .map(SamKakaoPredictPrice::getGeneratedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElseGet(() ->
                        ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime()
                                .toString()
                );

        // 5. 실제 + (있다면) 예측 합치기
        List<StockPriceRecordDTO> priceRecords = new ArrayList<>();
        priceRecords.addAll(actualRecords);      // 항상 3개 정도는 있음
        priceRecords.addAll(predictedRecords);   // 예측이 없으면 0개라서 그냥 실제만 나감

        // 6. 날짜순 정렬
        priceRecords.sort(Comparator.comparing(StockPriceRecordDTO::getDate));

        StockPredictResponseDTO response = new StockPredictResponseDTO(
                new StockInfoDTO(cleanedCode, stockName),
                priceRecords,
                new StockPredictMetaDTO(predictionGeneratedAt)
        );

        return List.of(response);
    }

    private List<StockPriceRecordDTO> fetchLast3BusinessDaysFromKrx(String stockCode) {
        List<StockPriceRecordDTO> results = new ArrayList<>();

        LocalDate requestDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (int i = 0; i < 15 && results.size() < 3; i++) {
            String dateString = requestDate.format(fmt);
            List<KrxStockInfo> dayInfos = krxApiClient.getStockInfo(dateString);

            if (dayInfos != null && !dayInfos.isEmpty()) {
                Optional<KrxStockInfo> targetOpt = dayInfos.stream()
                        .filter(info -> cleanCode(info.getStockCode()).equals(stockCode))
                        .findFirst();

                if (targetOpt.isPresent()) {
                    KrxStockInfo info = targetOpt.get();

                    // KRX 응답의 가격은 보통 "74,100" 이런 식이라 숫자만 뽑음
                    Integer close = Integer.valueOf(info.getCurrentPrice().replace(",", ""));

                    // 요청한 날짜를 그대로 ISO로
                    String isoDate = requestDate.toString(); // ex) 2025-11-04

                    results.add(
                            StockPriceRecordDTO.builder()
                                    .date(isoDate)
                                    .closePrice(close)
                                    .isPredicted(false)
                                    .build()
                    );
                }
            }

            // 하루 전으로 이동
            requestDate = requestDate.minusDays(1);
        }

        // 오래된 날짜부터 정렬해서 넘김
        results.sort(Comparator.comparing(StockPriceRecordDTO::getDate));
        return results;
    }

    private List<SamKakaoPredictPrice> fetchPredictionEntitiesFromDb(String stockCode) {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = nowSeoul.toLocalDate();
        LocalTime nowTime = nowSeoul.toLocalTime();

        LocalDate fromDate;
        if (nowTime.isAfter(LocalTime.of(18, 0))) {
            fromDate = today.plusDays(1);
        } else {
            fromDate = today;
        }

        return predictPriceRepository
                .findTop3ByStockCodeAndTargetDateGreaterThanEqualOrderByTargetDateAsc(stockCode, fromDate);
    }

    private String cleanCode(String code) {
        return code.replaceAll("[^0-9]", "");
    }
}
