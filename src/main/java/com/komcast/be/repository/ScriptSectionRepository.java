package com.komcast.be.repository;

import com.komcast.be.domain.ScriptSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScriptSectionRepository extends JpaRepository<ScriptSection, UUID> {
    List<ScriptSection> findByScriptIdOrderBySectionOrderAsc(UUID scriptId);
}
