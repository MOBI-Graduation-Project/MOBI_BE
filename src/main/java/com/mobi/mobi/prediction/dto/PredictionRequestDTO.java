
package com.mobi.mobi.prediction.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PredictionRequestDTO {

    private String marketName; // "KOSPI" 또는 "KOSDAQ"
    private String predictionResult; // "상승" 또는 "하락"
    private Double modelAccuracy;
    private LocalDate predictionDate; // "2025-11-05"
}