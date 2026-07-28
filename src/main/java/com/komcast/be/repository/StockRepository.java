package com.komcast.be.repository;

import com.komcast.be.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByStockCode(String stockCode);
    Optional<Stock> findByCorpName(String corpName);
}
