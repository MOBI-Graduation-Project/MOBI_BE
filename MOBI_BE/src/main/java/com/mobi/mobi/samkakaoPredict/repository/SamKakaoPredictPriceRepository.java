package com.mobi.mobi.samkakaoPredict.repository;

import com.mobi.mobi.samkakaoPredict.entity.SamKakaoPredictPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SamKakaoPredictPriceRepository
        extends JpaRepository<SamKakaoPredictPrice, Long> {

    List<SamKakaoPredictPrice> findTop3ByStockCodeAndTargetDateGreaterThanEqualOrderByTargetDateAsc(
            String stockCode,
            LocalDate fromDate
    );
}
