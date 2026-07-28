package com.komcast.be.repository;

import com.komcast.be.domain.SectionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectionLineRepository extends JpaRepository<SectionLine, UUID> {
    List<SectionLine> findBySectionIdOrderByLineOrderAsc(UUID sectionId);
}
