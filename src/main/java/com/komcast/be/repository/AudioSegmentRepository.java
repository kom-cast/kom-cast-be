package com.komcast.be.repository;

import com.komcast.be.domain.AudioSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AudioSegmentRepository extends JpaRepository<AudioSegment, UUID> {
    List<AudioSegment> findByAudioIdOrderBySegmentOrderAsc(UUID audioId);
}
