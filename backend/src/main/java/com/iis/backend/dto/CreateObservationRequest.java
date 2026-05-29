package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateObservationRequest(
        @NotNull Long playerId,
        @NotNull LocalDate observationDate,
        @NotBlank String period,
        String note) {
}
