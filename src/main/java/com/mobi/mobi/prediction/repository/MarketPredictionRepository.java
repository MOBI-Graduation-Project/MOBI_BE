package com.mobi.mobi.prediction.repository;

import com.mobi.mobi.prediction.entity.MarketPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketPredictionRepository extends JpaRepository<MarketPrediction, String> {
    Optional<MarketPrediction> findByMarketName(String marketName);
}