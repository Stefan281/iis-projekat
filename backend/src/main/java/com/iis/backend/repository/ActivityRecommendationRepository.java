package com.iis.backend.repository;

import com.iis.backend.model.ActivityRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRecommendationRepository extends JpaRepository<ActivityRecommendation, Long> {
    List<ActivityRecommendation> findByTeamAnalysisTeamStatisticMatchId(Long matchId);

    void deleteByTeamAnalysisId(Long teamAnalysisId);
}
