package com.iis.backend.repository;

import com.iis.backend.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    boolean existsByName(String name);
}
