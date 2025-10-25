package com.mobi.mobi.mydata.dto;

import com.mobi.mobi.mydata.entity.MyData;
import io.swagger.v3.oas.annotations.media.Schema; // Schema 임포트
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Schema(description = "개별 보유 주식 정보 응답 DTO")
public class MyDataResponseDTO {

    @Schema(description = "마이데이터 ID", example = "14")
    private final Long myDataId;

    @Schema(description = "종목 코드", example = "005930")
    private final String stockCode;

    @Schema(description = "매수 수량", example = "100")
    private final BigDecimal purchaseAmount;

    @Schema(description = "평균 매입 단가", example = "50000")
    private final BigDecimal avgPrice;

    @Schema(description = "종목명", example = "삼성전자")
    private final String stockName;

    @Schema(description = "현재가", example = "82500")
    @Setter
    private BigDecimal currentPrice;

    @Schema(description = "총 평가금액 (현재가 * 수량)", example = "8250000")
    @Setter
    private BigDecimal valuationAmount;

    @Schema(description = "개별 수익금 (평가금액 - 투자원금)", example = "3250000")
    @Setter
    private BigDecimal returnAmount;

    @Schema(description = "개별 수익률 (%)", example = "65.00")
    @Setter
    private BigDecimal returnRate;

    public MyDataResponseDTO(MyData myData) {
        this.myDataId = myData.getId();
        this.stockCode = myData.getStockData().getCode();
        this.stockName = myData.getStockData().getName();
        this.purchaseAmount = myData.getPurchaseAmount();
        this.avgPrice = myData.getAvgPrice();

    }
}