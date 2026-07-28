package com.komcast.be.repository;

import com.komcast.be.domain.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, String> {
    Optional<Industry> findByIndustryCode(String industryCode);
    Optional<Industry> findByIndustryName(String industryName);
}
