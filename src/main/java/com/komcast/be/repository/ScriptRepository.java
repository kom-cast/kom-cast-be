package com.komcast.be.repository;

import com.komcast.be.domain.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScriptRepository extends JpaRepository<Script, UUID> {
}
