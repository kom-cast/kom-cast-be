package com.komcast.be.repository;

import com.komcast.be.domain.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByStockCode(String stockCode);
    Optional<Stock> findByCorpName(String corpName);
    
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Stock s WHERE s.isKospi200 = true AND (s.corpName LIKE %:keyword% OR s.stockCode LIKE %:keyword%)")
    Page<Stock> searchKospi200Stocks(@org.springframework.data.repository.query.Param("keyword") String keyword, Pageable pageable);

    Page<Stock> findByIsKospi200True(Pageable pageable);
    java.util.List<Stock> findByIsKospi200True();
}
