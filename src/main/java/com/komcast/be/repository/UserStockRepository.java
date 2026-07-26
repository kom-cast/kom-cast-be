package com.komcast.be.repository;

import com.komcast.be.domain.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    List<UserStock> findByUserId(Long userId);
    List<UserStock> findByUserIdAndType(Long userId, String type);
    boolean existsByUserIdAndStockCode(Long userId, String stockCode);
    void deleteByUserIdAndStockCode(Long userId, String stockCode);
    void deleteByUserId(Long userId);
}
