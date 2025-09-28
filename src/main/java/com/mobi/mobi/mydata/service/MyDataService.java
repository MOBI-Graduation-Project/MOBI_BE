package com.mobi.mobi.mydata.service;

import com.mobi.mobi.apiPayload.handler.GeneralException;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
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

import java.util.List;
import java.util.stream.Collectors;
import com.mobi.mobi.stockdata.entity.StockData; // ✨ StockData 임포트
import com.mobi.mobi.stockdata.repository.StockDataRepository; // ✨ StockDataRepository 임포트


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataService {

    private final MyDataRepository myDataRepository;
    private final MemberRepository memberRepository;
    private final StockDataRepository stockDataRepository; // ✨ StockDataRepository 주입

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

    public MyDataListResponseDTO getMyData(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MyData> myDataList = myDataRepository.findAllByMember(member);
        List<MyDataResponseDTO> myDataResponseDTOList = myDataList.stream()
                .map(MyDataResponseDTO::new)
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
