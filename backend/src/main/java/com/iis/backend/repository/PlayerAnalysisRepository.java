package com.iis.backend.repository;

import com.iis.backend.model.PlayerAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerAnalysisRepository extends JpaRepository<PlayerAnalysis, Long> {
    Optional<PlayerAnalysis> findByPlayerStatisticId(Long playerStatisticId);

    void deleteByPlayerStatisticId(Long playerStatisticId);
}
