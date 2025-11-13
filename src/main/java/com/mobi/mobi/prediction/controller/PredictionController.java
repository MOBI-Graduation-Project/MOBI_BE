package com.mobi.mobi.prediction.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.prediction.dto.PredictionRequestDTO;
import com.mobi.mobi.prediction.dto.PredictionResponseDTO;
import com.mobi.mobi.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Market Prediction API", description = "시장(코스피/코스닥) 예측 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/prediction")
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping
    @Operation(summary = "시장 예측 결과 조회 API", description = "DB에 저장된 최신 코스피/코스닥 예측 결과를 조회합니다.")
    public ApiResponse<List<PredictionResponseDTO>> getPredictions() {
        List<PredictionResponseDTO> result = predictionService.getMarketPredictions();
        return ApiResponse.onSuccess(SuccessStatus._OK, result);
    }

    @PutMapping
    @Operation(summary = "시장 예측 결과 갱신 API", description = "AI가 매일 예측한 결과를 이 API를 통해 DB에 갱신(UPSERT)합니다.")
    public ApiResponse<String> updatePrediction(@RequestBody PredictionRequestDTO requestDTO) {

        predictionService.updateMarketPrediction(requestDTO);

        return ApiResponse.onSuccess(SuccessStatus._OK, "Prediction updated successfully.");
    }
}