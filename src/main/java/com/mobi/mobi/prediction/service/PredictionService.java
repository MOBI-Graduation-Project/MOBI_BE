package com.mobi.mobi.prediction.service;

import com.mobi.mobi.prediction.dto.PredictionRequestDTO;
import com.mobi.mobi.prediction.dto.PredictionResponseDTO;
import com.mobi.mobi.prediction.entity.MarketPrediction;
import com.mobi.mobi.prediction.repository.MarketPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (optionalPrediction.isPresent()) {
            // 이미 있으면 업데이트
            MarketPrediction existingPrediction = optionalPrediction.get();
            existingPrediction.updatePrediction(
                    requestDTO.getPredictionResult(),
                    requestDTO.getPredictionDate(),
                    requestDTO.getModelAccuracy()
            );
        } else {
            // 없으면 새로 생성
            MarketPrediction newPrediction = new MarketPrediction(
                    requestDTO.getMarketName(),
                    requestDTO.getPredictionResult(),
                    requestDTO.getPredictionDate(),
                    requestDTO.getModelAccuracy()
            );
            predictionRepository.save(newPrediction);
        }
    }
}
