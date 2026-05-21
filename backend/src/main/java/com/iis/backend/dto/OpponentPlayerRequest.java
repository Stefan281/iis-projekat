package com.iis.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpponentPlayerRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotNull @Min(0) @Max(99) Integer jerseyNumber,
        @NotBlank @Size(max = 80) String position,
        @NotNull @Min(120) @Max(250) Integer height,
        @NotNull @Min(12) @Max(60) Integer age) {
}
