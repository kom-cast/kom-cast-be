package com.komcast.be.repository;

import com.komcast.be.domain.UserIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserIndustryRepository extends JpaRepository<UserIndustry, UUID> {
    List<UserIndustry> findByUserId(UUID userId);
    boolean existsByUserIdAndIndustryCode(UUID userId, String industryCode);
    void deleteByUserIdAndIndustryCode(UUID userId, String industryCode);
    void deleteByUserId(UUID userId);
}
