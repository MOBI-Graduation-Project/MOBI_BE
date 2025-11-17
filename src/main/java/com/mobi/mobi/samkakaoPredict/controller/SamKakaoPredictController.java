package com.mobi.mobi.samkakaoPredict.controller;

import com.mobi.mobi.samkakaoPredict.dto.StockPredictResponseDTO;
import com.mobi.mobi.samkakaoPredict.service.SamKakaoPredictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prediction")
@RequiredArgsConstructor
@Tag(
        name = "개별 주식 예측",
        description = "과거 3일 + 미래 3일 예측 종가 조회"
)
public class SamKakaoPredictController {

    private final SamKakaoPredictService samKakaoPredictService;

    @Operation(
            summary = "주식 예측 조회",
            description = "과거 3일 실제 종가 + 미래 3일 예측 종가를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StockPredictResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "stock-predict-example",
                                            value = """
                                            [
                                              {
                                                "stock": {
                                                  "stockCode": "005930",
                                                  "stockName": "삼성전자"
                                                },
                                                "priceRecords": [
                                                  {
                                                    "date": "2025-11-05",
                                                    "closePrice": 98200,
                                                    "predictedPrice": 0,
                                                    "predicted": false
                                                  },
                                                  {
                                                    "date": "2025-11-06",
                                                    "closePrice": 97500,
                                                    "predictedPrice": 0,
                                                    "predicted": false
                                                  },
                                                  {
                                                    "date": "2025-11-07",
                                                    "closePrice": 99100,
                                                    "predictedPrice": 0,
                                                    "predicted": false
                                                  },
                                                  {
                                                    "date": "2025-11-10",
                                                    "closePrice": 0,
                                                    "predictedPrice": 99300,
                                                    "predicted": true
                                                  },
                                                  {
                                                    "date": "2025-11-11",
                                                    "closePrice": 0,
                                                    "predictedPrice": 100200,
                                                    "predicted": true
                                                  },
                                                  {
                                                    "date": "2025-11-12",
                                                    "closePrice": 0,
                                                    "predictedPrice": 99800,
                                                    "predicted": true
                                                  }
                                                ],
                                                "meta": {
                                                  "predictionGeneratedAt": "2025-11-08T18:00:00"
                                                }
                                              }
                                            ]
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{stockCode}")
    public List<StockPredictResponseDTO> getPrediction(@PathVariable String stockCode) {
        return samKakaoPredictService.getPrediction(stockCode);
    }
}
