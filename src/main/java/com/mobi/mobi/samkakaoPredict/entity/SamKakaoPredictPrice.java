package com.mobi.mobi.samkakaoPredict.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "samkakao_predict_price")
@Getter
@NoArgsConstructor
public class SamKakaoPredictPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "005930" 또는 "035720"
    @Column(nullable = false, length = 10)
    private String stockCode;

    // 예측 대상 날짜 (미래 날짜)
    @Column(nullable = false)
    private LocalDate targetDate;

    // 예측한 종가
    @Column(nullable = false)
    private Integer predictedPrice;

    // 이 예측이 만들어진 시각 (meta에 넣을 용도)
    private LocalDateTime generatedAt;

    public SamKakaoPredictPrice(String stockCode,
                                LocalDate targetDate,
                                Integer predictedPrice,
                                LocalDateTime generatedAt) {
        this.stockCode = stockCode;
        this.targetDate = targetDate;
        this.predictedPrice = predictedPrice;
        this.generatedAt = generatedAt;
    }
}
