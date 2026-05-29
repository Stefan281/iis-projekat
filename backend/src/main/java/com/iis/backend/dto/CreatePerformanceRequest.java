package com.iis.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePerformanceRequest(
        @NotNull Long playerId,
        @NotNull Long metricId,
        @NotNull BigDecimal value,
        String comment) {
}
