package com.iis.backend.dto;

import java.time.LocalDate;

public record ObservationResponse(
        Long id,
        Long playerId,
        String playerName,
        String position,
        LocalDate observationDate,
        String period,
        String note) {
}
