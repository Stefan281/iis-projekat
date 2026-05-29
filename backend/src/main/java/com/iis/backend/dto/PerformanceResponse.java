package com.iis.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PerformanceResponse(
        Long id,
        Long playerId,
        String playerName,
        String position,
        Long metricId,
        String metricName,
        String unitOfMeasure,
        BigDecimal value,
        String comment,
        LocalDateTime recordedAt) {
}
