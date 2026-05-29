package com.iis.backend.repository;

import com.iis.backend.model.PlayerAnalysis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PlayerAnalysisRepository extends JpaRepository<PlayerAnalysis, Long> {

    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "analyst"})
    List<PlayerAnalysis> findByPlayerId(Long playerId);

    List<PlayerAnalysis> findByAnalystId(Long analystId);

    @Override
    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "analyst"})
    List<PlayerAnalysis> findAll();
}
