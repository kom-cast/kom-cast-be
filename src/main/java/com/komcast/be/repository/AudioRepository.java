package com.komcast.be.repository;

import com.komcast.be.domain.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AudioRepository extends JpaRepository<Audio, UUID> {
    Optional<Audio> findTopByUserIdAndAudioTypeOrderByCreatedAtDesc(UUID userId, String audioType);
    org.springframework.data.domain.Page<Audio> findByUserIdAndAudioTypeOrderByCreatedAtDesc(UUID userId, String audioType, org.springframework.data.domain.Pageable pageable);
}
