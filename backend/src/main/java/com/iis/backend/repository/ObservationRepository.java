package com.iis.backend.repository;

import com.iis.backend.model.Observation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ObservationRepository extends JpaRepository<Observation, Long> {

    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "scout"})
    List<Observation> findByPlayerId(Long playerId);

    List<Observation> findByScoutId(Long scoutId);

    @Override
    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "scout"})
    List<Observation> findAll();
}
