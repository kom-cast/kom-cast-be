package com.komcast.be.repository;

import com.komcast.be.domain.UserIndustry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserIndustryRepository extends JpaRepository<UserIndustry, Long> {
    List<UserIndustry> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
