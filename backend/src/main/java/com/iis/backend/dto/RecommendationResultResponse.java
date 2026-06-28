package com.iis.backend.dto;

import com.iis.backend.model.RecommendationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecommendationResultResponse(
        Long id,
        Long modelId,
        Long playerId,
        String playerName,
        String position,
        String club,
        Integer height,
        BigDecimal score,
        RecommendationStatus status,
        String explanation,
        LocalDateTime calculatedAt) {
}
