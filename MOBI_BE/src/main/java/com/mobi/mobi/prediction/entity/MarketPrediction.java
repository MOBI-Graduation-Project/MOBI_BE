package com.mobi.mobi.prediction.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MarketPrediction extends BaseEntity {

    @Id
    @Column(name = "market_name", length = 50)
    private String marketName; // "KOSPI" 또는 "KOSDAQ"

    @Column(nullable = false)
    private String prediction;

    @Column
    private LocalDate predictionDate;

    @Column
    private Double modelAccuracy;
    public void updatePrediction(String prediction, LocalDate predictionDate, Double modelAccuracy) {
        this.prediction = prediction;
        this.predictionDate = predictionDate;
        this.modelAccuracy = modelAccuracy;

    }

    public MarketPrediction(String marketName, String prediction, LocalDate predictionDate, Double modelAccuracy) {
        this.marketName = marketName;
        this.prediction = prediction;
        this.predictionDate = predictionDate;
        this.modelAccuracy = modelAccuracy;
    }

}