package com.mobi.mobi.stockdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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


    @Column(name = "listing_date")
    private LocalDate listingDate;


    @Builder
    public StockData(String code, String name, String market, LocalDate listingDate) {
        this.code = code;
        this.name = name;
        this.market = market;
        this.listingDate = listingDate;
    }
}