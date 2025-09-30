package com.mobi.mobi.external.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class KrxItems {
    @JsonProperty("item")
    private List<KrxStockInfo> itemList;
}