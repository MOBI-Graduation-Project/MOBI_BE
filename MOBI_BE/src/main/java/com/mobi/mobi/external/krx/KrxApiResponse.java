package com.mobi.mobi.external.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class KrxApiResponse {

    @JsonProperty("OutBlock_1")
    private List<KrxStockInfo> outBlock1;

}