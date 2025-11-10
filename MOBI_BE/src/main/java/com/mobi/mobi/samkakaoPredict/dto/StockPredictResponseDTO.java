package com.mobi.mobi.samkakaoPredict.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StockPredictResponseDTO {

    private StockInfoDTO stock;
    private List<StockPriceRecordDTO> priceRecords;
    private StockPredictMetaDTO meta;
}
