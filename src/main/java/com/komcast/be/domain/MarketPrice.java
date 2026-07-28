package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_prices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "traded_at", nullable = false)
    private ZonedDateTime tradedAt;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false)
    private BigDecimal closePrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "change_rate", nullable = false)
    private BigDecimal changeRate;
}
