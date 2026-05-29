package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreatePlayerAnalysisRequest(
        @NotNull Long playerId,
        @NotNull LocalDate analysisDate,
        @NotBlank String conclusion,
        String note) {
}
