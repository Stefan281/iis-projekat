package com.iis.backend.dto;

import com.iis.backend.model.EventType;
import com.iis.backend.model.MatchEvent;
import java.time.LocalDateTime;

public record MatchEventResponse(
        Long id,
        EventType eventType,
        LocalDateTime eventTime,
        String description,
        Long statisticianId,
        Long primaryPlayerId,
        Long secondaryPlayerId,
        String playerName,
        Integer jerseyNumber,
        String teamName) {

    public static MatchEventResponse fromEntity(MatchEvent event) {
        var player = event.getPrimaryPlayer();

        return new MatchEventResponse(
                event.getId(),
                event.getEventType(),
                event.getEventTime(),
                event.getDescription(),
                event.getStatistician().getId(),
                player.getId(),
                event.getSecondaryPlayer() == null ? null : event.getSecondaryPlayer().getId(),
                player.getFullName(),
                player.getJerseyNumber(),
                player.getTeam().getName());
    }
}
