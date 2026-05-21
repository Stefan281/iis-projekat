package com.iis.backend.repository;

import com.iis.backend.model.OpponentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpponentTeamRepository extends JpaRepository<OpponentTeam, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
