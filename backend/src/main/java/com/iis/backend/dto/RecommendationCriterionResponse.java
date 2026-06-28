package com.iis.backend.dto;

import com.iis.backend.model.CriterionComparison;
import java.math.BigDecimal;

public record RecommendationCriterionResponse(
        Long id,
        Long metricId,
        String metricName,
        String unitOfMeasure,
        CriterionComparison comparison,
        BigDecimal thresholdValue,
        BigDecimal weight,
        boolean required) {
}
