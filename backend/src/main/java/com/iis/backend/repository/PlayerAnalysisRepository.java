package com.iis.backend.repository;

import com.iis.backend.model.PlayerAnalysis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerAnalysisRepository extends JpaRepository<PlayerAnalysis, Long> {

    List<PlayerAnalysis> findByPlayerId(Long playerId);

    List<PlayerAnalysis> findByAnalystId(Long analystId);
}
