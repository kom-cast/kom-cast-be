package com.komcast.be.repository;

import com.komcast.be.domain.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    List<UserStock> findByUserId(Long userId);
    List<UserStock> findByUserIdAndType(Long userId, String type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserStock u WHERE u.user.id = :userId AND u.stockCode = :stockCode")
    void deleteByUserIdAndStockCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserStock u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
