package com.mobi.mobi.mydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "파이 차트 전용 데이터 DTO")
public class PieChartDTO {

    @Schema(description = "종목 코드", example = "005930")
    private final String stockCode;

    @Schema(description = "종목명 (파이 차트 라벨)", example = "삼성전자")
    private final String stockName;

    @Schema(description = "포트폴리오 내 보유 비중 (%)", example = "21")
    private final BigDecimal holdingWeight;
}