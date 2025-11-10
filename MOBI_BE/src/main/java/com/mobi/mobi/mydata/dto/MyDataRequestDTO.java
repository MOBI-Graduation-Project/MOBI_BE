package com.mobi.mobi.mydata.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class MyDataRequestDTO {


    @NotBlank(message = "종목명을 입력해주세요.")
    private String stockName;

    @NotNull(message = "매수량을 입력해주세요.")
    @Positive(message = "매수량은 0보다 커야 합니다.")
    @Digits(integer = 14, fraction = 4, message = "매수량 형식이 올바르지 않습니다.")
    private BigDecimal purchaseAmount;

    @NotNull(message = "평균 매입 단가를 입력해주세요.")
    @Positive(message = "평균 매입 단가는 0보다 커야 합니다.")
    @Digits(integer = 14, fraction = 4, message = "평균 매입 단가 형식이 올바르지 않습니다.")
    private BigDecimal avgPrice;
}