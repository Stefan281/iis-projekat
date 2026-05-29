package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreatePlayerRecommendationRequest(
        @NotNull Long playerId,
        Long analysisId,
        @NotNull LocalDate recommendationDate,
        String criteria,
        @NotBlank String explanation) {
}
