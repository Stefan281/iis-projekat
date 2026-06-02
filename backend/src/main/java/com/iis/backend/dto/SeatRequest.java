package com.iis.backend.dto;

import com.iis.backend.model.SeatStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatRequest(
        @NotBlank String rowLabel,
        @NotNull @Min(1) Integer seatNumber,
        @NotNull SeatStatus status,
        @NotNull Long zoneId) {
}
