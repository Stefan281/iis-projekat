package com.iis.backend.dto;

import com.iis.backend.model.PlayerStatistic;
import com.iis.backend.model.TeamAnalysis;
import java.util.Map;

public record TeamAnalysisResponse(
        Long teamId,
        String teamName,
        Integer teamEfficiency,
        Integer attackIndex,
        Integer serveIndex,
        Integer blockIndex,
        Integer disciplineIndex,
        AnalysisPlayerResponse mostEfficientPlayer,
        AnalysisPlayerResponse leastEfficientPlayer,
        AnalysisPlayerResponse topPointsPlayer,
        AnalysisPlayerResponse topErrorsPlayer,
        AnalysisPlayerResponse topBlocksPlayer,
        AnalysisPlayerResponse topServesPlayer,
        AnalysisPlayerResponse topAssistsPlayer) {

    public static TeamAnalysisResponse fromEntity(TeamAnalysis analysis, Map<Long, PlayerStatistic> playerStatistics) {
        var statistic = analysis.getTeamStatistic();

        return new TeamAnalysisResponse(
                statistic.getTeam().getId(),
                statistic.getTeam().getName(),
                analysis.getTeamEfficiency(),
                analysis.getAttackIndex(),
                analysis.getServeIndex(),
                analysis.getBlockIndex(),
                analysis.getDisciplineIndex(),
                playerResponse(analysis.getMostEfficientPlayer(), playerStatistics),
                playerResponse(analysis.getLeastEfficientPlayer(), playerStatistics),
                playerResponse(analysis.getTopPointsPlayer(), playerStatistics),
                playerResponse(analysis.getTopErrorsPlayer(), playerStatistics),
                playerResponse(analysis.getTopBlocksPlayer(), playerStatistics),
                playerResponse(analysis.getTopServesPlayer(), playerStatistics),
                playerResponse(analysis.getTopAssistsPlayer(), playerStatistics));
    }

    private static AnalysisPlayerResponse playerResponse(
            com.iis.backend.model.OpponentPlayer player,
            Map<Long, PlayerStatistic> playerStatistics) {
        if (player == null) {
            return null;
        }

        var statistic = playerStatistics.get(player.getId());
        if (statistic == null) {
            return AnalysisPlayerResponse.fromValues(player, 0, 0, 0, 0, 0, 0);
        }

        return AnalysisPlayerResponse.fromValues(
                player,
                statistic.getPoints(),
                statistic.getErrors(),
                statistic.getBlocks(),
                statistic.getServes(),
                statistic.getAssists(),
                statistic.getPoints() * 2
                        + statistic.getBlocks() * 2
                        + statistic.getAssists()
                        + statistic.getServes()
                        - statistic.getErrors() * 2);
    }
}
