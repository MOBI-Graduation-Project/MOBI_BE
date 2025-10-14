package com.mobi.mobi.external.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class KrxApiResponse {

    // 명세서에 따르면, 응답 JSON의 최상위에는 "OutBlock_1" 배열만 존재합니다.
    @JsonProperty("OutBlock_1")
    private List<KrxStockInfo> outBlock1;

    // "header"는 별도로 존재하지 않습니다. 성공 여부는 데이터 유무로 판단합니다.
}