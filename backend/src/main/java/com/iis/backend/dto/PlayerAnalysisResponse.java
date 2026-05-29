package com.iis.backend.dto;

import java.time.LocalDate;

public record PlayerAnalysisResponse(
        Long id,
        Long playerId,
        String playerName,
        String position,
        LocalDate analysisDate,
        String conclusion,
        String note,
        String analystName) {
}
