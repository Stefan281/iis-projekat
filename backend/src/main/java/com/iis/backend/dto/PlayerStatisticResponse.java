package com.iis.backend.dto;

import com.iis.backend.model.PlayerStatistic;

public record PlayerStatisticResponse(
        Long playerId,
        String playerName,
        Integer jerseyNumber,
        String playerStatus,
        Integer points,
        Integer errors,
        Integer blocks,
        Integer serves,
        Integer assists) {

    public static PlayerStatisticResponse fromEntity(PlayerStatistic statistic) {
        return new PlayerStatisticResponse(
                statistic.getPlayer().getId(),
                statistic.getPlayer().getFullName(),
                statistic.getPlayer().getJerseyNumber(),
                statistic.getPlayer().getPlayerStatus().name(),
                statistic.getPoints(),
                statistic.getErrors(),
                statistic.getBlocks(),
                statistic.getServes(),
                statistic.getAssists());
    }
}
