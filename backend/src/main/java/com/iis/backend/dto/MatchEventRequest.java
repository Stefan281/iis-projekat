package com.iis.backend.dto;

import com.iis.backend.model.EventType;
import jakarta.validation.constraints.NotNull;

public record MatchEventRequest(
        @NotNull Long statisticianId,
        @NotNull Long primaryPlayerId,
        Long secondaryPlayerId,
        @NotNull EventType eventType) {
}
