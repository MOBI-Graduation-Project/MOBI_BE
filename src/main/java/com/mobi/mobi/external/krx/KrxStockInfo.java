package com.mobi.mobi.external.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KrxStockInfo {
    @JsonProperty("TDD_CLSPRC") // 종가 (오늘의 현재가)
    private String currentPrice;

    @JsonProperty("ISU_CD") // 종목 코드
    private String stockCode;
}