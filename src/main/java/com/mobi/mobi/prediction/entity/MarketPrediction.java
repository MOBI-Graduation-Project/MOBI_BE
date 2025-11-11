package com.mobi.mobi.prediction.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MarketPrediction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_name", length = 50, nullable = false)
    private String marketName;

    @Column(name = "prediction_result", nullable = false)
    private String prediction;

    @Column(name = "prediction_date")
    private LocalDate predictionDate;

    @Column(name = "model_accuracy")
    private Double modelAccuracy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public void updatePrediction(String prediction,
                                 LocalDate predictionDate,
                                 Double modelAccuracy,
                                 LocalDateTime generatedAt) {
        this.prediction = prediction;
        this.predictionDate = predictionDate;
        this.modelAccuracy = modelAccuracy;
        this.generatedAt = generatedAt;
    }

    public MarketPrediction(String marketName,
                            String prediction,
                            LocalDate predictionDate,
                            Double modelAccuracy,
                            LocalDateTime generatedAt) {
        this.marketName = marketName;
        this.prediction = prediction;
        this.predictionDate = predictionDate;
        this.modelAccuracy = modelAccuracy;
        this.generatedAt = generatedAt;
    }
}
