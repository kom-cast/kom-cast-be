package com.komcast.be.repository;

import com.komcast.be.domain.UserSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSectorRepository extends JpaRepository<UserSector, Long> {
    List<UserSector> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
