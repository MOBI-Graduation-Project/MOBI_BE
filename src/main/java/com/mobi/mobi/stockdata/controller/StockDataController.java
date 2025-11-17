package com.mobi.mobi.stockdata.controller;

import com.mobi.mobi.stockdata.dto.StockSearchResponse;
import com.mobi.mobi.stockdata.service.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stockdata") // API 공통 주소 설정
public class StockDataController {

    private final StockDataService stockDataService;

    // API 최종 주소: /api/stockdata/search?keyword=검색어
    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> searchStocks(@RequestParam String keyword) {
        return ResponseEntity.ok(stockDataService.searchStocks(keyword));
    }
}