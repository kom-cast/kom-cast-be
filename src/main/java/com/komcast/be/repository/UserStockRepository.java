package com.komcast.be.repository;

import com.komcast.be.domain.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, UUID> {
    List<UserStock> findByUserId(UUID userId);
    List<UserStock> findByUserIdAndType(UUID userId, String type);
    boolean existsByUserIdAndStockCode(UUID userId, String stockCode);
    void deleteByUserIdAndStockCode(UUID userId, String stockCode);
    void deleteByUserId(UUID userId);
}
