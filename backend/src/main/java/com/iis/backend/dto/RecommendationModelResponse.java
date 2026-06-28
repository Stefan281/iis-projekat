package com.iis.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RecommendationModelResponse(
        Long id,
        String name,
        String position,
        Integer minimumHeight,
        BigDecimal minimumScore,
        String createdByName,
        LocalDateTime createdAt,
        List<RecommendationCriterionResponse> criteria) {
}
