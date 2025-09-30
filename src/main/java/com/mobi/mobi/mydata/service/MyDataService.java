package com.mobi.mobi.mydata.service;

import com.mobi.mobi.apiPayload.handler.GeneralException;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.external.krx.KrxApiClient;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.mydata.dto.MyDataListResponseDTO;
import com.mobi.mobi.mydata.dto.MyDataRequestDTO;
import com.mobi.mobi.mydata.dto.MyDataResponseDTO;
import com.mobi.mobi.mydata.entity.MyData;
import com.mobi.mobi.mydata.repository.MyDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map; // ✨ Map 임포트
import java.math.BigDecimal; // ✨ BigDecimal 임포트
import java.time.LocalDate; // ✨ LocalDate 임포트
import java.time.format.DateTimeFormatter; // ✨ DateTimeFormatter 임포트
import java.util.Collections; // ✨ Collections 임포트
import java.util.List;
import java.util.stream.Collectors;
import com.mobi.mobi.stockdata.entity.StockData; // ✨ StockData 임포트
import com.mobi.mobi.stockdata.repository.StockDataRepository; // ✨ StockDataRepository 임포트
import com.mobi.mobi.external.krx.KrxStockInfo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataService {

    private final MyDataRepository myDataRepository;
    private final MemberRepository memberRepository;
    private final StockDataRepository stockDataRepository; // 주식 종목코드랑 종목명 조회
    private final KrxApiClient krxApiClient; // 현재가


    @Transactional
    public MyDataResponseDTO addMyData(Long memberId, MyDataRequestDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 이미 해당 주식 코드가 등록되어 있는지 확인
      //  if (myDataRepository.existsByMemberAndStockCode(member, requestDTO.getStockCode())) {
      //      throw new GeneralException(ErrorStatus.MYDATA_ALREADY_EXISTS);
      //  }
        // ▼▼▼ [변경] 로직 수정 ▼▼▼
        // 1. 요청으로 들어온 stockCode가 DB에 존재하는지 확인
        StockData stockData = stockDataRepository.findById(requestDTO.getStockCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND)); // STOCK_NOT_FOUND 에러 필요

        // 2. 이미 해당 주식을 등록했는지 확인
        if (myDataRepository.existsByMemberAndStockData(member, stockData)) {
            throw new GeneralException(ErrorStatus.MYDATA_ALREADY_EXISTS);
        }

        MyData myData = MyData.builder()
                .member(member)
                .stockData(stockData) // String 대신 찾은 StockData 객체를 전달.stockCode(requestDTO.getStockCode())
                .purchaseAmount(requestDTO.getPurchaseAmount())
                .avgPrice(requestDTO.getAvgPrice())
                .build();

        MyData savedMyData = myDataRepository.save(myData);
        return new MyDataResponseDTO(savedMyData);
    }

    /*
    public MyDataListResponseDTO getMyData(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MyData> myDataList = myDataRepository.findAllByMember(member);
        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(MyDataResponseDTO::new)
                .collect(Collectors.toList());

        return new MyDataListResponseDTO(myDataResponseDTOList);
    }
    */
    public MyDataListResponseDTO getMyData(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // N+1 문제 해결을 위해 fetch join 사용 권장
        List<MyData> myDataList = myDataRepository.findAllByMemberWithStockData(member);

        // 사용자가 등록한 주식이 없으면 API를 호출할 필요가 없으므로 바로 반환
        if (myDataList.isEmpty()) {
            return new MyDataListResponseDTO(Collections.emptyList());
        }

        // 1. 오늘 날짜를 "yyyyMMdd" 형식으로 생성
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. KRX API를 호출해 오늘 날짜의 모든 주식 정보를 가져옴
        List<KrxStockInfo> allStockInfo = krxApiClient.getStockInfo(today);

        // 3. 조회된 주식 정보를 빠르게 찾을 수 있도록 Map으로 변환 (Key: 종목코드, Value: 현재가)
        Map<String, String> priceMap = allStockInfo.stream()
                .collect(Collectors.toMap(KrxStockInfo::getStockCode, KrxStockInfo::getCurrentPrice));

        // 4. 사용자의 보유 주식 목록(DTO)에 현재가 정보를 추가
        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(myData -> {
                    MyDataResponseDTO dto = new MyDataResponseDTO(myData);
                    String currentPriceStr = priceMap.get(myData.getStockData().getCode());
                    if (currentPriceStr != null && !currentPriceStr.isEmpty()) {
                        // API 응답의 가격은 쉼표(,)가 포함된 문자열일 수 있으므로 제거 후 숫자로 변환
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
        MyData myData = myDataRepository.findByIdAndMemberId(myDataId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MYDATA_NOT_FOUND));

        // 주식 코드는 변경 불가, 매수량과 평균 단가만 업데이트
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
