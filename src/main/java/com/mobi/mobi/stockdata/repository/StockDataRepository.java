package com.mobi.mobi.stockdata.repository;

import com.mobi.mobi.stockdata.entity.StockData; // import 경로 추가
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockDataRepository extends JpaRepository<StockData, String> {

    List<StockData> findByNameContaining(String keyword);
}