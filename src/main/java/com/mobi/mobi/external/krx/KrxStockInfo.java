package com.mobi.mobi.external.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KrxStockInfo {

    @JsonProperty("ISU_CD")     // 명세서의 "종목코드"
    private String stockCode;

    @JsonProperty("TDD_CLSPRC") // 명세서의 "종가" (오늘의 현재가)
    private String currentPrice;
}