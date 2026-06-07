package com.iis.backend.dto;

import com.iis.backend.model.ActivityRecommendation;
import java.time.LocalDateTime;

public record ActivityRecommendationResponse(
        Long id,
        Long teamId,
        String teamName,
        String teamType,
        Long playerId,
        String playerName,
        Integer jerseyNumber,
        String type,
        String title,
        String description,
        String priority,
        LocalDateTime createdAt) {

    public static ActivityRecommendationResponse fromEntity(ActivityRecommendation recommendation) {
        var team = recommendation.getTeamAnalysis().getTeamStatistic().getTeam();
        var player = recommendation.getPlayer();

        return new ActivityRecommendationResponse(
                recommendation.getId(),
                team.getId(),
                team.getName(),
                team.getTeamType().name(),
                player == null ? null : player.getId(),
                player == null ? null : player.getFullName(),
                player == null ? null : player.getJerseyNumber(),
                recommendation.getType().name(),
                recommendation.getTitle(),
                recommendation.getDescription(),
                recommendation.getPriority().name(),
                recommendation.getCreatedAt());
    }
}
