package com.iis.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record MatchStatisticsResponse(
        Long matchId,
        LocalDate matchDate,
        String result,
        String status,
        TeamStatisticResponse homeTeam,
        TeamStatisticResponse awayTeam,
        List<PlayerStatisticResponse> homePlayers,
        List<PlayerStatisticResponse> awayPlayers,
        TeamAnalysisResponse homeAnalysis,
        TeamAnalysisResponse awayAnalysis,
        List<PlayerAnalysisResponse> homePlayerAnalyses,
        List<PlayerAnalysisResponse> awayPlayerAnalyses,
        List<ActivityRecommendationResponse> recommendations) {
}
