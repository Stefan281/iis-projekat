package com.iis.backend.repository;

import com.iis.backend.model.OpponentTeam;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpponentTeamRepository extends JpaRepository<OpponentTeam, Long> {
    @EntityGraph(attributePaths = "players")
    Optional<OpponentTeam> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
