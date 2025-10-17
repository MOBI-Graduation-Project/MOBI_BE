package com.mobi.mobi.stockdata.dto;

import com.mobi.mobi.stockdata.entity.StockData;
import lombok.Getter;

@Getter
public class StockSearchResponse {
    private String code;
    private String name;

    public StockSearchResponse(StockData stockData) {
        this.code = stockData.getCode();
        this.name = stockData.getName();
    }
}