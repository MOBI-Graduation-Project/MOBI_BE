package com.mobi.mobi.stockdata.repository;

import com.mobi.mobi.stockdata.entity.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // ✨ List 임포트 추가
import java.util.Optional;

public interface StockDataRepository extends JpaRepository<StockData, String> {

    // 1. MyDataService를 위한 메소드 (정확히 일치하는 이름 찾기)
    Optional<StockData> findByName(String name);

    // 2. SajuService를 위한 메소드 (이름을 포함하는 모든 결과 찾기)
    List<StockData> findByNameContaining(String name);
}