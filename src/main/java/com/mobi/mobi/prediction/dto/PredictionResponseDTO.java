package com.mobi.mobi.prediction.dto;

import com.mobi.mobi.prediction.entity.MarketPrediction;
import lombok.Getter;
import java.time.LocalDate; // 수정
import java.time.LocalDateTime;

@Getter
public class PredictionResponseDTO {
    private final String marketName;
    private final String prediction;
    private final LocalDateTime lastUpdated;

    private final LocalDate predictionDate; // 예측 기준일
    private final Double modelAccuracy; // 모델 정확도


    public PredictionResponseDTO(MarketPrediction prediction) {
        this.marketName = prediction.getMarketName();
        this.prediction = prediction.getPrediction();
        this.lastUpdated = prediction.getUpdatedAt();

        this.predictionDate = prediction.getPredictionDate();
        this.modelAccuracy = prediction.getModelAccuracy();

    }
}