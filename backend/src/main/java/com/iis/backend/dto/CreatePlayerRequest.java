package com.iis.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePlayerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Min(1950) @Max(2020) Integer birthYear,
        @NotNull @Min(120) @Max(230) Integer height,
        @NotBlank String position,
        @NotBlank String club,
        String note) {
}
