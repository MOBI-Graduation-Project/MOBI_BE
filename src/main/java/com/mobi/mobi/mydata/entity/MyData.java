package com.mobi.mobi.mydata.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import com.mobi.mobi.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mydata_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "stock_code", nullable = false, length = 16)
    private String stockCode;

    @Column(name = "purchase_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal purchaseAmount;

    @Column(name = "avg_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgPrice;


    @Builder
    public MyData(Member member, String stockCode, BigDecimal purchaseAmount, BigDecimal avgPrice) {
        this.member = member;
        this.stockCode = stockCode;
        this.purchaseAmount = purchaseAmount;
        this.avgPrice = avgPrice;
    }

    //== 비즈니스 로직 추가 ==//
    public void update(BigDecimal purchaseAmount, BigDecimal avgPrice) {
        this.purchaseAmount = purchaseAmount;
        this.avgPrice = avgPrice;
    }
}
