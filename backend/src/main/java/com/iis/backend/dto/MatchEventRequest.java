package com.iis.backend.dto;

import com.iis.backend.model.EventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatchEventRequest(
        @NotNull Long statisticianId,
        @NotNull Long primaryPlayerId,
        Long secondaryPlayerId,
        @NotNull EventType eventType,
        @Size(max = 500) String description) {
}
