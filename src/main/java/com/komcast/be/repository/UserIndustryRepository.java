package com.komcast.be.repository;

import com.komcast.be.domain.UserIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserIndustryRepository extends JpaRepository<UserIndustry, Long> {
    List<UserIndustry> findByUserId(Long userId);
    boolean existsByUserIdAndIndustryCode(Long userId, String industryCode);
    void deleteByUserIdAndIndustryCode(Long userId, String industryCode);
    void deleteByUserId(Long userId);
}
