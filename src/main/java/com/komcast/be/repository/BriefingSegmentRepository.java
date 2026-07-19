package com.komcast.be.repository;

import com.komcast.be.domain.BriefingSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BriefingSegmentRepository extends JpaRepository<BriefingSegment, Long> {
    List<BriefingSegment> findByBriefingIdOrderByFractionAsc(Long briefingId);
}
