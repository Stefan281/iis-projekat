package com.iis.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record RecommendationModelRequest(
        @NotBlank String name,
        String position,
        @Positive Integer minimumHeight,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal minimumScore,
        @NotEmpty List<@Valid RecommendationCriterionRequest> criteria) {
}
