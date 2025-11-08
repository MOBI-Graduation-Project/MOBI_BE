package com.mobi.mobi.mydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 보유 주식 목록 및 포트폴리오 요약 응답 DTO")
public class MyDataListResponseDTO {

    @Schema(description = "개별 보유 주식 목록")
    private List<MyDataResponseDTO> myDataList;

    @Schema(description = "파이 차트 전용 데이터 리스트")
    private List<PieChartDTO> pieChart;

    @Schema(description = "총 평가금액 (포트폴리오 전체)", example = "15000000")
    private BigDecimal totalValuationAmount;

    @Schema(description = "총 투자원금 (포트폴리오 전체)", example = "10000000")
    private BigDecimal totalPrincipalAmount;

    @Schema(description = "총 수익금 (포트폴리오 전체)", example = "5000000")
    private BigDecimal totalReturnAmount;

    @Schema(description = "총 수익률 (%) (포트폴리오 전체)", example = "50.00")
    private BigDecimal totalReturnRate;
}