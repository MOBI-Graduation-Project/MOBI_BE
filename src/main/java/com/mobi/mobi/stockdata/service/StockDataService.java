package com.mobi.mobi.stockdata.service;

import com.mobi.mobi.stockdata.dto.StockSearchResponse;
import com.mobi.mobi.stockdata.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockDataService {

    private final StockDataRepository stockDataRepository;

    public List<StockSearchResponse> searchStocks(String keyword) {
        return stockDataRepository.findByNameContaining(keyword)
                .stream()
                .map(StockSearchResponse::new)
                .collect(Collectors.toList());
    }
}