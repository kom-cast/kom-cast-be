package com.komcast.be.repository;

import com.komcast.be.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUserId(Long userId);
    List<UserKeyword> findByUserIdAndType(Long userId, String type);
    void deleteByUserId(Long userId);
}
