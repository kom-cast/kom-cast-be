package com.komcast.be.repository;

import com.komcast.be.domain.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, UUID> {
    Optional<MarketPrice> findTopByStockCodeOrderByTradedAtDesc(String stockCode);
}
