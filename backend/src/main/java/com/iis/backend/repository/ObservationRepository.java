package com.iis.backend.repository;

import com.iis.backend.model.Observation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationRepository extends JpaRepository<Observation, Long> {

    List<Observation> findByPlayerId(Long playerId);

    List<Observation> findByScoutId(Long scoutId);
}
