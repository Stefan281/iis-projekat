package com.iis.backend.dto;

import java.time.LocalDate;

public record PlayerRecommendationResponse(
        Long id,
        Long playerId,
        String playerName,
        String position,
        Long analysisId,
        String analysisConclusion,
        LocalDate recommendationDate,
        String criteria,
        String explanation,
        String recommendedByName) {
}
