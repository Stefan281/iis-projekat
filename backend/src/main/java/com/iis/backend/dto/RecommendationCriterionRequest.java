package com.iis.backend.dto;

import com.iis.backend.model.CriterionComparison;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecommendationCriterionRequest(
        @NotNull Long metricId,
        @NotNull CriterionComparison comparison,
        @NotNull @DecimalMin("0.01") BigDecimal thresholdValue,
        @NotNull @DecimalMin("0.01") BigDecimal weight,
        boolean required) {
}
