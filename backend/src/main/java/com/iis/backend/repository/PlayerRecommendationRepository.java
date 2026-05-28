package com.iis.backend.repository;

import com.iis.backend.model.PlayerRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRecommendationRepository extends JpaRepository<PlayerRecommendation, Long> {

    List<PlayerRecommendation> findByPlayerId(Long playerId);

    List<PlayerRecommendation> findByRecommendedById(Long recommendedById);
}
