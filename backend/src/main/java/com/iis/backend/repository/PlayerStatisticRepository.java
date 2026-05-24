package com.iis.backend.repository;

import com.iis.backend.model.PlayerStatistic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {
    Optional<PlayerStatistic> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    List<PlayerStatistic> findByMatchIdAndPlayerTeamIdOrderByPlayerJerseyNumberAsc(Long matchId, Long teamId);
}
