package com.iis.backend.dto;

import com.iis.backend.model.OpponentPlayer;

public record AnalysisPlayerResponse(
        Long playerId,
        String playerName,
        Integer jerseyNumber,
        Integer points,
        Integer errors,
        Integer blocks,
        Integer serves,
        Integer assists,
        Integer efficiency) {

    public static AnalysisPlayerResponse fromValues(
            OpponentPlayer player,
            Integer points,
            Integer errors,
            Integer blocks,
            Integer serves,
            Integer assists,
            Integer efficiency) {
        if (player == null) {
            return null;
        }

        return new AnalysisPlayerResponse(
                player.getId(),
                player.getFullName(),
                player.getJerseyNumber(),
                points,
                errors,
                blocks,
                serves,
                assists,
                efficiency);
    }
}
