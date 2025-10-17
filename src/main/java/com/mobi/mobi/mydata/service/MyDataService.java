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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        // ... (이 메소드는 기존과 동일)
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MyData> myDataList = myDataRepository.findAllByMemberWithStockData(member);

        if (myDataList.isEmpty()) {

            return new MyDataListResponseDTO(Collections.emptyList(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }


        LocalDate requestDate = LocalDate.now();
        List<KrxStockInfo> allStockInfo = Collections.emptyList();

        System.out.println("===== KRX API 호출 시작 ====="); // 디버깅 로그
        for (int i = 0; i < 15; i++) {
            String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            System.out.println("날짜 " + dateString + "으로 데이터 조회 시도..."); // 디버깅 로그

            allStockInfo = krxApiClient.getStockInfo(dateString);

            if (allStockInfo != null && !allStockInfo.isEmpty()) {
                System.out.println("성공! " + dateString + " 날짜에서 " + allStockInfo.size() + "개의 주식 정보를 찾았습니다."); // 디버깅 로그
                break;
            }

            requestDate = requestDate.minusDays(1);
        }
        // -------------------------------------------------------------------
        System.out.println("===== KRX API 호출 종료 ====="); // 디버깅 로그


        Map<String, String> priceMap = Objects.requireNonNull(allStockInfo).stream()
                .collect(Collectors.toMap(
                        stockInfo -> stockInfo.getStockCode().replaceAll("[^0-9]", ""), // "A005930" -> "005930"
                        KrxStockInfo::getCurrentPrice,
                        (price1, price2) -> price1 // 중복 키 발생 시 첫 번째 값 사용
                ));

        // 디버깅: priceMap에 어떤 데이터가 들어있는지 확인
        if (!priceMap.isEmpty()) {
            System.out.println("Price Map에 저장된 첫번째 데이터 -> Key: " + priceMap.keySet().iterator().next() + ", Value: " + priceMap.values().iterator().next());
        } else {
            System.out.println("Price Map이 비어있습니다. API로부터 유효한 데이터를 받지 못했을 수 있습니다.");
        }


        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(myData -> {
                    MyDataResponseDTO dto = new MyDataResponseDTO(myData);
                    String currentPriceStr = priceMap.get(myData.getStockData().getCode());

                    if (currentPriceStr != null && !currentPriceStr.isEmpty()) {
                        BigDecimal currentPrice = new BigDecimal(currentPriceStr.replace(",", ""));
                        dto.setCurrentPrice(currentPrice);

                        BigDecimal avgPrice = myData.getAvgPrice();
                        BigDecimal purchaseAmount = myData.getPurchaseAmount();

                        // 1. 개별 수익금 계산: (현재가 - 평단가) * 수량
                        BigDecimal returnAmount = (currentPrice.subtract(avgPrice)).multiply(purchaseAmount);
                        dto.setReturnAmount(returnAmount);

                        // 2. 개별 수익률 계산: ((현재가 / 평단가) - 1) * 100
                        if (avgPrice.compareTo(BigDecimal.ZERO) > 0) { // 평단가가 0이 아닐 때만 계산
                            BigDecimal returnRate = (currentPrice.divide(avgPrice, 4, RoundingMode.HALF_UP))
                                    .subtract(BigDecimal.ONE)
                                    .multiply(new BigDecimal("100"));
                            dto.setReturnRate(returnRate);
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 3. 전체 포트폴리오 요약 계산
        BigDecimal totalValuationAmount = BigDecimal.ZERO; // 총 평가금액
        BigDecimal totalPrincipalAmount = BigDecimal.ZERO; // 총 투자원금

        for (MyDataResponseDTO dto : myDataResponseDTOList) {
            if (dto.getCurrentPrice() != null) {
                totalValuationAmount = totalValuationAmount.add(dto.getCurrentPrice().multiply(dto.getPurchaseAmount()));
            }
            totalPrincipalAmount = totalPrincipalAmount.add(dto.getAvgPrice().multiply(dto.getPurchaseAmount()));
        }

        BigDecimal totalReturnAmount = totalValuationAmount.subtract(totalPrincipalAmount); // 총 수익금
        BigDecimal totalReturnRate = BigDecimal.ZERO;
        if (totalPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnRate = (totalValuationAmount.divide(totalPrincipalAmount, 4, RoundingMode.HALF_UP))
                    .subtract(BigDecimal.ONE)
                    .multiply(new BigDecimal("100"));
        }

        return new MyDataListResponseDTO(myDataResponseDTOList, totalValuationAmount, totalPrincipalAmount, totalReturnAmount, totalReturnRate);
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
}