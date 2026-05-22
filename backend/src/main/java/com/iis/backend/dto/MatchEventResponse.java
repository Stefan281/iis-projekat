package com.iis.backend.dto;

import com.iis.backend.model.EventType;
import com.iis.backend.model.MatchEvent;
import java.time.LocalDateTime;

public record MatchEventResponse(
        Long id,
        EventType eventType,
        LocalDateTime eventTime,
        Long statisticianId,
        Long primaryPlayerId,
        Long secondaryPlayerId,
        String playerName,
        String secondaryPlayerName,
        Integer jerseyNumber,
        Integer secondaryJerseyNumber,
        String teamName) {

    public static MatchEventResponse fromEntity(MatchEvent event) {
        var player = event.getPrimaryPlayer();
        var secondaryPlayer = event.getSecondaryPlayer();

        return new MatchEventResponse(
                event.getId(),
                event.getEventType(),
                event.getEventTime(),
                event.getStatistician().getId(),
                player.getId(),
                secondaryPlayer == null ? null : secondaryPlayer.getId(),
                player.getFullName(),
                secondaryPlayer == null ? null : secondaryPlayer.getFullName(),
                player.getJerseyNumber(),
                secondaryPlayer == null ? null : secondaryPlayer.getJerseyNumber(),
                player.getTeam().getName());
    }
}
