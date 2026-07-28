package com.komcast.be.repository;

import com.komcast.be.domain.Briefing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BriefingRepository extends JpaRepository<Briefing, UUID> {
    Optional<Briefing> findTopByUserIdAndDateOrderByCreatedAtDesc(UUID userId, LocalDate date);
    Page<Briefing> findByUserIdOrderByDateDesc(UUID userId, Pageable pageable);
}
