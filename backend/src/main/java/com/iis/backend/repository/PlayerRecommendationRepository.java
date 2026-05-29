package com.iis.backend.repository;

import com.iis.backend.model.PlayerRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PlayerRecommendationRepository extends JpaRepository<PlayerRecommendation, Long> {

    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "analysis", "recommendedBy"})
    List<PlayerRecommendation> findByPlayerId(Long playerId);

    List<PlayerRecommendation> findByRecommendedById(Long recommendedById);

    @Override
    @EntityGraph(attributePaths = {"player", "player.position", "player.club", "analysis", "recommendedBy"})
    List<PlayerRecommendation> findAll();
}
