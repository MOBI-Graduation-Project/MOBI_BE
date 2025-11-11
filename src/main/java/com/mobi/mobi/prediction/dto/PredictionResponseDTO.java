package com.mobi.mobi.prediction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobi.mobi.prediction.entity.MarketPrediction;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PredictionResponseDTO {

    private final String marketName;

    @JsonProperty("prediction_result")
    private final String predictionResult;

    @JsonProperty("lastUpdated")
    private final LocalDateTime lastUpdated;

    @JsonProperty("predictionDate")
    private final LocalDate predictionDate; // 예측 기준일

    private final Double modelAccuracy; // 모델 정확도


    public PredictionResponseDTO(MarketPrediction prediction) {
        this.marketName = prediction.getMarketName();
        this.predictionResult = prediction.getPrediction();
        this.lastUpdated = prediction.getUpdatedAt();
        this.predictionDate = prediction.getPredictionDate();
        this.modelAccuracy = prediction.getModelAccuracy();
    }

    private PredictionResponseDTO(String marketName,
                                  String predictionResult,
                                  LocalDateTime lastUpdated,
                                  LocalDate predictionDate,
                                  Double modelAccuracy) {
        this.marketName = marketName;
        this.predictionResult = predictionResult;
        this.lastUpdated = lastUpdated;
        this.predictionDate = predictionDate;
        this.modelAccuracy = modelAccuracy;
    }

    public static PredictionResponseDTO defaultOf(String marketName) {
        double defaultAccuracy;

        if ("KOSPI".equalsIgnoreCase(marketName)) {
            defaultAccuracy = 56.78;
        } else if ("KOSDAQ".equalsIgnoreCase(marketName)) {
            defaultAccuracy = 55.32;
        } else {
            defaultAccuracy = 0.0;
        }

        return new PredictionResponseDTO(
                marketName,
                "상승",
                LocalDateTime.now(),
                LocalDate.now().plusDays(1),
                defaultAccuracy
        );
    }
}
