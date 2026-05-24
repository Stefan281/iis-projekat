package com.iis.backend.dto;

import com.iis.backend.model.TeamStatistic;

public record TeamStatisticResponse(
        Long teamId,
        String teamName,
        Integer setsWon,
        Integer points,
        Integer errors,
        Integer serves,
        Integer blocks,
        Integer substitutions) {

    public static TeamStatisticResponse fromEntity(TeamStatistic statistic) {
        return new TeamStatisticResponse(
                statistic.getTeam().getId(),
                statistic.getTeam().getName(),
                statistic.getSetsWon(),
                statistic.getPoints(),
                statistic.getErrors(),
                statistic.getServes(),
                statistic.getBlocks(),
                statistic.getSubstitutions());
    }
}
