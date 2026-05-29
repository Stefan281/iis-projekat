package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMetricRequest(
        @NotBlank String name,
        @NotNull Boolean standardMetric,
        @NotBlank String unitOfMeasure,
        String description) {
}
