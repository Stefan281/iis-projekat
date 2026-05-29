package com.iis.backend.repository;

import com.iis.backend.model.PerformanceValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PerformanceValueRepository extends JpaRepository<PerformanceValue, Long> {

    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "metric", "recordedBy"})
    List<PerformanceValue> findByPlayerId(Long playerId);

    List<PerformanceValue> findByMetricId(Long metricId);

    @Override
    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "metric", "recordedBy"})
    List<PerformanceValue> findAll();
}
