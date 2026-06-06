package com.iis.backend.repository;

import com.iis.backend.model.TeamAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAnalysisRepository extends JpaRepository<TeamAnalysis, Long> {
    Optional<TeamAnalysis> findByTeamStatisticId(Long teamStatisticId);

    void deleteByTeamStatisticId(Long teamStatisticId);
}
