package com.mobi.mobi.samkakaoPredict.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockPriceRecordDTO {

    private String date;

    // 실제 종가일 때만 값이 있음
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer closePrice;

    // 예측 종가일 때만 값이 있음
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer predictedPrice;

    private boolean isPredicted;
}
