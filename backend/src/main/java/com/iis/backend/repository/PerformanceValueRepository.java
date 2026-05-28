package com.iis.backend.repository;

import com.iis.backend.model.PerformanceValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceValueRepository extends JpaRepository<PerformanceValue, Long> {

    List<PerformanceValue> findByPlayerId(Long playerId);

    List<PerformanceValue> findByMetricId(Long metricId);
}
