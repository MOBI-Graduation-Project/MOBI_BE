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

    public MyDataResponseDTO(MyData myData) {
        this.myDataId = myData.getId();
        this.memberId = myData.getMember().getId();
        this.stockCode = myData.getStockCode();
        this.purchaseAmount = myData.getPurchaseAmount();
        this.avgPrice = myData.getAvgPrice();
    }
}
