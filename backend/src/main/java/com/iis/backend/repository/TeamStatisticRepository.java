package com.iis.backend.repository;

import com.iis.backend.model.TeamStatistic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long> {
    Optional<TeamStatistic> findByMatchIdAndTeamId(Long matchId, Long teamId);

    boolean existsByMatchId(Long matchId);
}
