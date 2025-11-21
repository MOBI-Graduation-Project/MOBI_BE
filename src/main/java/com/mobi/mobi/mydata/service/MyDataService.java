package com.mobi.mobi.mydata.service;

import com.mobi.mobi.apiPayload.handler.GeneralException;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.external.krx.KrxApiClient;
import com.mobi.mobi.external.krx.KrxStockInfo;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.mydata.dto.MyDataListResponseDTO;
import com.mobi.mobi.mydata.dto.MyDataRequestDTO;
import com.mobi.mobi.mydata.dto.MyDataResponseDTO;
import com.mobi.mobi.mydata.dto.PieChartDTO;
import com.mobi.mobi.mydata.entity.MyData;
import com.mobi.mobi.mydata.repository.MyDataRepository;
import com.mobi.mobi.stockdata.entity.StockData;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataService {

    private final MyDataRepository myDataRepository;
    private final MemberRepository memberRepository;
    private final StockDataRepository stockDataRepository;
    private final KrxApiClient krxApiClient;

    @Transactional
    public MyDataResponseDTO addMyData(Long memberId, MyDataRequestDTO requestDTO) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String cleanedStockName = requestDTO.getStockName().trim();
        StockData stockData = stockDataRepository.findByName(cleanedStockName)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        if (myDataRepository.existsByMemberAndStockData(member, stockData)) {
            throw new GeneralException(ErrorStatus.MYDATA_ALREADY_EXISTS);
        }

        MyData myData = MyData.builder()
                .member(member)
                .stockData(stockData)
                .purchaseAmount(requestDTO.getPurchaseAmount())
                .avgPrice(requestDTO.getAvgPrice())
                .build();

        MyData savedMyData = myDataRepository.save(myData);
        return new MyDataResponseDTO(savedMyData);
    }

    public MyDataListResponseDTO getMyData(Long memberId) {

        // 공통 계산 로직
        PortfolioCalcResult calc = loadAndCalculatePortfolio(memberId);

        return new MyDataListResponseDTO(
                calc.myDataResponseDTOList,
                calc.pieChartList,                 // 기존 응답 포맷 유지
                calc.totalValuationAmount,
                calc.totalPrincipalAmount,
                calc.totalReturnAmount,
                calc.totalReturnRate
        );
    }

    public List<PieChartDTO> getMyDataPieChart(Long memberId) {
        PortfolioCalcResult calc = loadAndCalculatePortfolio(memberId);
        return calc.pieChartList;
    }

    @Transactional
    public MyDataResponseDTO updateMyData(Long memberId, Long myDataId, MyDataRequestDTO requestDTO) {

        MyData myData = myDataRepository.findByIdAndMemberId(myDataId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MYDATA_NOT_FOUND));

        myData.update(requestDTO.getPurchaseAmount(), requestDTO.getAvgPrice());
        return new MyDataResponseDTO(myData);
    }

    @Transactional
    public void deleteMyData(Long memberId, Long myDataId) {

        MyData myData = myDataRepository.findByIdAndMemberId(myDataId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MYDATA_NOT_FOUND));
        myDataRepository.delete(myData);
    }

    private PortfolioCalcResult loadAndCalculatePortfolio(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MyData> myDataList = myDataRepository.findAllByMemberWithStockData(member);

        // 보유종목이 아예 없으면 빈 값들로 리턴
        if (myDataList.isEmpty()) {
            return new PortfolioCalcResult(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        // 1. KRX 데이터 가져오기 (최근 날짜부터 최대 15일 뒤로)
        LocalDate requestDate = LocalDate.now();
        List<KrxStockInfo> allStockInfo = Collections.emptyList();

        for (int i = 0; i < 15; i++) {
            String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            allStockInfo = krxApiClient.getStockInfo(dateString);
            if (allStockInfo != null && !allStockInfo.isEmpty()) {
                break;
            }
            requestDate = requestDate.minusDays(1);
        }

        // 2. 주가 맵으로 변환
        Map<String, String> priceMap = Objects.requireNonNull(allStockInfo).stream()
                .collect(Collectors.toMap(
                        stockInfo -> stockInfo.getStockCode().replaceAll("[^0-9]", ""),
                        KrxStockInfo::getCurrentPrice,
                        (price1, price2) -> price1
                ));

        // 3. MyData -> DTO 변환 + 개별 계산
        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(myData -> {
                    MyDataResponseDTO dto = new MyDataResponseDTO(myData);

                    String currentPriceStr = priceMap.get(myData.getStockData().getCode());

                    BigDecimal avgPrice = myData.getAvgPrice();
                    BigDecimal purchaseAmount = myData.getPurchaseAmount();
                    BigDecimal principalAmount = avgPrice.multiply(purchaseAmount); // 개별 투자원금

                    if (currentPriceStr != null && !currentPriceStr.isEmpty()) {
                        BigDecimal currentPrice = new BigDecimal(currentPriceStr.replace(",", ""));
                        dto.setCurrentPrice(currentPrice);

                        BigDecimal valuationAmount = currentPrice.multiply(purchaseAmount); // 개별 평가금액
                        dto.setValuationAmount(valuationAmount);

                        BigDecimal returnAmount = valuationAmount.subtract(principalAmount); // 개별 수익금
                        dto.setReturnAmount(returnAmount);

                        if (principalAmount.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal returnRate = (valuationAmount.divide(principalAmount, 4, RoundingMode.HALF_UP))
                                    .subtract(BigDecimal.ONE)
                                    .multiply(new BigDecimal("100"));
                            dto.setReturnRate(returnRate);
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 4. 전체 합계 계산
        BigDecimal totalValuationAmount = BigDecimal.ZERO;
        BigDecimal totalPrincipalAmount = BigDecimal.ZERO;

        for (MyDataResponseDTO dto : myDataResponseDTOList) {
            if (dto.getValuationAmount() != null) {
                totalValuationAmount = totalValuationAmount.add(dto.getValuationAmount());
            }
            totalPrincipalAmount = totalPrincipalAmount.add(dto.getAvgPrice().multiply(dto.getPurchaseAmount()));
        }

        // 5. 개별 비중 계산 (정수로)
        if (totalValuationAmount.compareTo(BigDecimal.ZERO) > 0) {
            for (MyDataResponseDTO dto : myDataResponseDTOList) {
                if (dto.getValuationAmount() != null) {
                    // 1. (평가금액 / 총평가금액) * 100 계산
                    // 2. setScale(2, RoundingMode.HALF_UP)으로 소수점 2자리 반올림 확정
                    BigDecimal weight = dto.getValuationAmount()
                            .multiply(new BigDecimal("100"))
                            .divide(totalValuationAmount, 4, RoundingMode.HALF_UP) // 나눗셈은 넉넉하게 4자리까지
                            .setScale(2, RoundingMode.HALF_UP); // 최종 결과는 2자리로 자름 (예: 21.357 -> 21.36)

                    dto.setHoldingWeight(weight);
                } else {
                    dto.setHoldingWeight(BigDecimal.ZERO.setScale(2)); // 0.00 으로 설정
                }
            }
        }

        // 6. 파이차트용으로 종목코드 단위로 합치기
        Map<String, BigDecimal> weightByStock = new LinkedHashMap<>();
        Map<String, String> nameByStock = new HashMap<>();

        for (MyDataResponseDTO dto : myDataResponseDTOList) {
            String stockCode = dto.getStockCode();
            String stockName = dto.getStockName();
            BigDecimal weight = dto.getHoldingWeight() != null ? dto.getHoldingWeight() : BigDecimal.ZERO;

            nameByStock.putIfAbsent(stockCode, stockName);
            weightByStock.merge(stockCode, weight, BigDecimal::add);
        }

        List<PieChartDTO> pieChartList = weightByStock.entrySet().stream()
                .map(e -> PieChartDTO.builder()
                        .stockCode(e.getKey())
                        .stockName(nameByStock.get(e.getKey()))
                        .holdingWeight(e.getValue())
                        .build())
                .toList();

        // 7. 총 수익금/수익률
        BigDecimal totalReturnAmount = totalValuationAmount.subtract(totalPrincipalAmount);
        BigDecimal totalReturnRate = BigDecimal.ZERO;
        if (totalPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnRate = (totalValuationAmount.divide(totalPrincipalAmount, 4, RoundingMode.HALF_UP))
                    .subtract(BigDecimal.ONE)
                    .multiply(new BigDecimal("100"));
        }

        return new PortfolioCalcResult(
                myDataResponseDTOList,
                pieChartList,
                totalValuationAmount,
                totalPrincipalAmount,
                totalReturnAmount,
                totalReturnRate
        );
    }

    private record PortfolioCalcResult(
            List<MyDataResponseDTO> myDataResponseDTOList,
            List<PieChartDTO> pieChartList,
            BigDecimal totalValuationAmount,
            BigDecimal totalPrincipalAmount,
            BigDecimal totalReturnAmount,
            BigDecimal totalReturnRate
    ) {}
}
