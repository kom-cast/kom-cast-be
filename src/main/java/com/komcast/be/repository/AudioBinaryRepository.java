package com.komcast.be.repository;

import com.komcast.be.domain.AudioBinary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AudioBinaryRepository extends JpaRepository<AudioBinary, UUID> {
}
