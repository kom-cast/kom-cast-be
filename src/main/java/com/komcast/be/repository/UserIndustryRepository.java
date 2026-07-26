package com.komcast.be.repository;

import com.komcast.be.domain.UserIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserIndustryRepository extends JpaRepository<UserIndustry, Long> {
    List<UserIndustry> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserIndustry u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
