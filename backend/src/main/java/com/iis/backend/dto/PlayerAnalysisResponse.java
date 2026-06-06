package com.iis.backend.dto;

import com.iis.backend.model.PlayerAnalysis;

public record PlayerAnalysisResponse(
        Long playerId,
        String playerName,
        Integer jerseyNumber,
        Integer efficiency,
        Integer serveContribution,
        Integer overallRating) {

    public static PlayerAnalysisResponse fromEntity(PlayerAnalysis analysis) {
        var statistic = analysis.getPlayerStatistic();
        var player = statistic.getPlayer();

        return new PlayerAnalysisResponse(
                player.getId(),
                player.getFullName(),
                player.getJerseyNumber(),
                analysis.getEfficiency(),
                analysis.getServeContribution(),
                analysis.getOverallRating());
    }
}
