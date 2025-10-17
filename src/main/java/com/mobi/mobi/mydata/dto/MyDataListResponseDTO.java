package com.mobi.mobi.mydata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyDataListResponseDTO {

    private List<MyDataResponseDTO> myDataList;

    private BigDecimal totalValuationAmount; // 총 평가금액
    private BigDecimal totalPrincipalAmount; // 총 투자원금
    private BigDecimal totalReturnAmount;      // 총 수익금
    private BigDecimal totalReturnRate;        // 총 수익률 (%)
}
