package com.mobi.mobi.mydata.dto;


import com.mobi.mobi.mydata.entity.MyData;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
public class MyDataResponseDTO {

    private final Long myDataId;
    private final Long memberId;
    private final String stockCode;
    private final BigDecimal purchaseAmount;
    private final BigDecimal avgPrice;
    private final String stockName; // 종목 이름도 어짜피 csv파일에 있어서 불러옴
    // 주식 현재가
    @Setter
    private BigDecimal currentPrice;

    @Setter
    private BigDecimal returnAmount; // 수익금

    @Setter
    private BigDecimal returnRate;   // 수익률 (%)

    public MyDataResponseDTO(MyData myData) {
        this.myDataId = myData.getId();
        this.memberId = myData.getMember().getId();
        // this.stockCode = myData.getStockCode();
        this.stockCode = myData.getStockData().getCode(); // stockData 객체에서 코드 가져오기
        this.stockName = myData.getStockData().getName(); // stockData 객체에서 이름 가져오기
        this.purchaseAmount = myData.getPurchaseAmount();
        this.avgPrice = myData.getAvgPrice();
    }

}
