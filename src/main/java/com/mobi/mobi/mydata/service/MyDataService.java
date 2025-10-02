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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        StockData stockData = stockDataRepository.findById(requestDTO.getStockCode())
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

    // ▼▼▼▼▼ 휴일 처리 로직이 훨씬 더 안정적으로 개선되었습니다 ▼▼▼▼▼
    public MyDataListResponseDTO getMyData(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MyData> myDataList = myDataRepository.findAllByMemberWithStockData(member);

        if (myDataList.isEmpty()) {
            return new MyDataListResponseDTO(Collections.emptyList());
        }

        // --- [개선] 데이터가 있을 때까지 하루씩 이전 날짜로 API를 호출하는 로직 ---
        LocalDate requestDate = LocalDate.now();
        List<KrxStockInfo> allStockInfo = Collections.emptyList();

        for (int i = 0; i < 7; i++) { // 최대 7일 전까지만 조회 (무한 루프 방지)
            String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            allStockInfo = krxApiClient.getStockInfo(dateString);

            if (!allStockInfo.isEmpty()) {
                break; // 데이터를 찾았으면 반복 중단
            }

            requestDate = requestDate.minusDays(1); // 데이터를 못 찾았으면 하루 전으로
        }
        // -------------------------------------------------------------------

        Map<String, String> priceMap = allStockInfo.stream()
                .collect(Collectors.toMap(KrxStockInfo::getStockCode, KrxStockInfo::getCurrentPrice));

        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(myData -> {
                    MyDataResponseDTO dto = new MyDataResponseDTO(myData);
                    String currentPriceStr = priceMap.get(myData.getStockData().getCode());
                    if (currentPriceStr != null && !currentPriceStr.isEmpty()) {
                        BigDecimal currentPrice = new BigDecimal(currentPriceStr.replace(",", ""));
                        dto.setCurrentPrice(currentPrice);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new MyDataListResponseDTO(myDataResponseDTOList);
    }

    @Transactional
    public MyDataResponseDTO updateMyData(Long memberId, Long myDataId, MyDataRequestDTO requestDTO) {
        // ... (이 메소드는 기존과 동일)
        MyData myData = myDataRepository.findByIdAndMemberId(myDataId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MYDATA_NOT_FOUND));

        myData.update(requestDTO.getPurchaseAmount(), requestDTO.getAvgPrice());
        return new MyDataResponseDTO(myData);
    }

    @Transactional
    public void deleteMyData(Long memberId, Long myDataId) {
        // ... (이 메소드는 기존과 동일)
        MyData myData = myDataRepository.findByIdAndMemberId(myDataId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MYDATA_NOT_FOUND));
        myDataRepository.delete(myData);
    }
}