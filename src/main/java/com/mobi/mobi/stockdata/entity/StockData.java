package com.mobi.mobi.stockdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate; // LocalDate가 이미 임포트되어 있는지 확인

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StockData {

    @Id
    @Column(name = "stock_code", nullable = false, unique = true)
    private String code;

    @Column(name = "stock_name", nullable = false)
    private String name;

    @Column(name = "stock_market")
    private String market;

    // ▼▼▼ [추가] 상장일 필드 ▼▼▼
    @Column(name = "listing_date")
    private LocalDate listingDate;

    // ▼▼▼ [수정] Builder와 생성자에 listingDate 추가 ▼▼▼
    @Builder
    public StockData(String code, String name, String market, LocalDate listingDate) {
        this.code = code;
        this.name = name;
        this.market = market;
        this.listingDate = listingDate;
    }
}