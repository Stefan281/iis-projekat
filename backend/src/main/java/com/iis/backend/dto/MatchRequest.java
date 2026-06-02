package com.iis.backend.dto;

import com.iis.backend.model.MatchAttractiveness;
import com.iis.backend.model.MatchStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record MatchRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotBlank String homeTeam,
        @NotBlank String awayTeam,
        @NotBlank String location,
        @NotNull MatchStatus status,
        @NotNull @DecimalMin("0.0") BigDecimal basePrice,
        @NotNull MatchAttractiveness attractiveness,
        @NotNull @Min(0) Integer expectedAttendance) {
}
