package com.mobi.mobi.mydata.dto;


import com.mobi.mobi.mydata.entity.MyData;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MyDataResponseDTO {

    private final Long myDataId;
    private final Long memberId;
    private final String stockCode;
    private final BigDecimal purchaseAmount;
    private final BigDecimal avgPrice;
    private final String stockName; // ✨ 종목명 필드 추가

    public MyDataResponseDTO(MyData myData) {
        this.myDataId = myData.getId();
        this.memberId = myData.getMember().getId();
        // this.stockCode = myData.getStockCode();
        this.stockCode = myData.getStockData().getCode(); // ✨ stockData 객체에서 코드 가져오기
        this.stockName = myData.getStockData().getName(); // ✨ stockData 객체에서 이름 가져오기
        this.purchaseAmount = myData.getPurchaseAmount();
        this.avgPrice = myData.getAvgPrice();
    }
}
