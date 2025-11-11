package com.mobi.mobi.prediction.service;

import com.mobi.mobi.prediction.dto.PredictionRequestDTO;
import com.mobi.mobi.prediction.dto.PredictionResponseDTO;
import com.mobi.mobi.prediction.entity.MarketPrediction;
import com.mobi.mobi.prediction.repository.MarketPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionService {

    private final MarketPredictionRepository predictionRepository;

    public List<PredictionResponseDTO> getMarketPredictions() {
        return Stream.of("KOSPI", "KOSDAQ")
                .map(this::findOrDefault)
                .collect(Collectors.toList());
    }


    private PredictionResponseDTO findOrDefault(String marketName) {
        return predictionRepository.findByMarketName(marketName)
                .map(PredictionResponseDTO::new)
                .orElseGet(() -> PredictionResponseDTO.defaultOf(marketName));
    }


    @Transactional
    public void updateMarketPrediction(PredictionRequestDTO requestDTO) {

        Optional<MarketPrediction> optionalPrediction =
                predictionRepository.findByMarketName(requestDTO.getMarketName());

        LocalDateTime now = LocalDateTime.now();

        if (optionalPrediction.isPresent()) {

            MarketPrediction existingPrediction = optionalPrediction.get();
            existingPrediction.updatePrediction(
                    requestDTO.getPredictionResult(),   // "상승"/"하락"
                    requestDTO.getPredictionDate(),     // 예측 날짜
                    requestDTO.getModelAccuracy(),      // 모델 정확도
                    now                                 // generated_at 갱신
            );
        } else {

            MarketPrediction newPrediction = new MarketPrediction(
                    requestDTO.getMarketName(),
                    requestDTO.getPredictionResult(),
                    requestDTO.getPredictionDate(),
                    requestDTO.getModelAccuracy(),
                    now
            );
            predictionRepository.save(newPrediction);
        }
    }
}
