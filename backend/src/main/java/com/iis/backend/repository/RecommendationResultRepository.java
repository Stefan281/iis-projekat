package com.iis.backend.repository;

import com.iis.backend.model.RecommendationResult;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {

    @EntityGraph(attributePaths = {"model", "player", "player.position", "player.club"})
    List<RecommendationResult> findByModelIdOrderByScoreDesc(Long modelId);

    void deleteByModelId(Long modelId);
}
