package com.iis.backend.service;

import com.iis.backend.dto.MatchStatisticsResponse;
import com.iis.backend.dto.PlayerAnalysisResponse;
import com.iis.backend.dto.PlayerStatisticResponse;
import com.iis.backend.dto.TeamAnalysisResponse;
import com.iis.backend.dto.TeamStatisticResponse;
import com.iis.backend.model.EventType;
import com.iis.backend.model.Match;
import com.iis.backend.model.MatchEvent;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.PlayerAnalysis;
import com.iis.backend.model.PlayerStatistic;
import com.iis.backend.model.TeamAnalysis;
import com.iis.backend.model.TeamStatistic;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.PlayerAnalysisRepository;
import com.iis.backend.repository.PlayerStatisticRepository;
import com.iis.backend.repository.TeamAnalysisRepository;
import com.iis.backend.repository.TeamStatisticRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchStatisticsService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final TeamStatisticRepository teamStatisticRepository;
    private final PlayerStatisticRepository playerStatisticRepository;
    private final TeamAnalysisRepository teamAnalysisRepository;
    private final PlayerAnalysisRepository playerAnalysisRepository;

    public MatchStatisticsService(
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            TeamStatisticRepository teamStatisticRepository,
            PlayerStatisticRepository playerStatisticRepository,
            TeamAnalysisRepository teamAnalysisRepository,
            PlayerAnalysisRepository playerAnalysisRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.teamStatisticRepository = teamStatisticRepository;
        this.playerStatisticRepository = playerStatisticRepository;
        this.teamAnalysisRepository = teamAnalysisRepository;
        this.playerAnalysisRepository = playerAnalysisRepository;
    }

    @Transactional
    public MatchStatisticsResponse getCurrentMatchStatistics() {
        var match = matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema aktivne utakmice."));

        backfillStatisticsIfNeeded(match);

        var homeTeamStatistic = getOrCreateTeamStatistic(match, match.getHomeTeam());
        var awayTeamStatistic = getOrCreateTeamStatistic(match, match.getAwayTeam());
        regenerateMatchAnalyses(match);

        return new MatchStatisticsResponse(
                match.getId(),
                match.getMatchDate(),
                match.getResult(),
                match.getStatus().name(),
                TeamStatisticResponse.fromEntity(homeTeamStatistic),
                TeamStatisticResponse.fromEntity(awayTeamStatistic),
                getPlayerStatistics(match, match.getHomeTeam()),
                getPlayerStatistics(match, match.getAwayTeam()),
                getTeamAnalysis(match, homeTeamStatistic),
                getTeamAnalysis(match, awayTeamStatistic),
                getPlayerAnalyses(match, match.getHomeTeam()),
                getPlayerAnalyses(match, match.getAwayTeam()));
    }

    public void applyEvent(MatchEvent event) {
        updateEventStatistics(event, 1);
    }

    public void revertEvent(MatchEvent event) {
        updateEventStatistics(event, -1);
    }

    private void updateEventStatistics(MatchEvent event, int delta) {
        var player = event.getPrimaryPlayer();
        var teamStatistic = getOrCreateTeamStatistic(event.getMatch(), player.getTeam());

        switch (event.getEventType()) {
            case POINT -> {
                var playerStatistic = getOrCreatePlayerStatistic(event.getMatch(), player);
                teamStatistic.setPoints(adjust(teamStatistic.getPoints(), delta));
                playerStatistic.setPoints(adjust(playerStatistic.getPoints(), delta));
                playerStatisticRepository.save(playerStatistic);
            }
            case ERROR -> {
                var playerStatistic = getOrCreatePlayerStatistic(event.getMatch(), player);
                teamStatistic.setErrors(adjust(teamStatistic.getErrors(), delta));
                playerStatistic.setErrors(adjust(playerStatistic.getErrors(), delta));
                playerStatisticRepository.save(playerStatistic);
            }
            case SERVE -> {
                var playerStatistic = getOrCreatePlayerStatistic(event.getMatch(), player);
                teamStatistic.setServes(adjust(teamStatistic.getServes(), delta));
                playerStatistic.setServes(adjust(playerStatistic.getServes(), delta));
                playerStatisticRepository.save(playerStatistic);
            }
            case BLOCK -> {
                var playerStatistic = getOrCreatePlayerStatistic(event.getMatch(), player);
                teamStatistic.setBlocks(adjust(teamStatistic.getBlocks(), delta));
                playerStatistic.setBlocks(adjust(playerStatistic.getBlocks(), delta));
                playerStatisticRepository.save(playerStatistic);
            }
            case ASSIST -> {
                var playerStatistic = getOrCreatePlayerStatistic(event.getMatch(), player);
                playerStatistic.setAssists(adjust(playerStatistic.getAssists(), delta));
                playerStatisticRepository.save(playerStatistic);
            }
            case SUBSTITUTION -> teamStatistic.setSubstitutions(adjust(teamStatistic.getSubstitutions(), delta));
        }

        teamStatisticRepository.save(teamStatistic);
        regenerateMatchAnalyses(event.getMatch());
    }

    private TeamStatistic getOrCreateTeamStatistic(Match match, OpponentTeam team) {
        return teamStatisticRepository.findByMatchIdAndTeamId(match.getId(), team.getId())
                .orElseGet(() -> {
                    var statistic = new TeamStatistic();
                    statistic.setMatch(match);
                    statistic.setTeam(team);
                    statistic.setSetsWon(getSetsWon(match, team));
                    return teamStatisticRepository.save(statistic);
                });
    }

    private PlayerStatistic getOrCreatePlayerStatistic(Match match, OpponentPlayer player) {
        return playerStatisticRepository.findByMatchIdAndPlayerId(match.getId(), player.getId())
                .orElseGet(() -> {
                    var statistic = new PlayerStatistic();
                    statistic.setMatch(match);
                    statistic.setPlayer(player);
                    return playerStatisticRepository.save(statistic);
                });
    }

    private List<PlayerStatisticResponse> getPlayerStatistics(Match match, OpponentTeam team) {
        return team.getPlayers().stream()
                .sorted(Comparator.comparing(OpponentPlayer::getJerseyNumber))
                .map(player -> getOrCreatePlayerStatistic(match, player))
                .map(PlayerStatisticResponse::fromEntity)
                .toList();
    }

    private List<PlayerAnalysisResponse> getPlayerAnalyses(Match match, OpponentTeam team) {
        return playerStatisticRepository.findByMatchIdAndPlayerTeamIdOrderByPlayerJerseyNumberAsc(
                        match.getId(), team.getId())
                .stream()
                .map(statistic -> playerAnalysisRepository.findByPlayerStatisticId(statistic.getId()))
                .flatMap(java.util.Optional::stream)
                .map(PlayerAnalysisResponse::fromEntity)
                .toList();
    }

    private TeamAnalysisResponse getTeamAnalysis(Match match, TeamStatistic statistic) {
        return teamAnalysisRepository.findByTeamStatisticId(statistic.getId())
                .map(analysis -> TeamAnalysisResponse.fromEntity(analysis, playerStatisticMap(match, statistic.getTeam())))
                .orElse(null);
    }

    private void regenerateMatchAnalyses(Match match) {
        regenerateTeamAnalyses(match, match.getHomeTeam());
        regenerateTeamAnalyses(match, match.getAwayTeam());
    }

    private void regenerateTeamAnalyses(Match match, OpponentTeam team) {
        var teamStatistic = getOrCreateTeamStatistic(match, team);
        var playerStatistics = playerStatisticRepository.findByMatchIdAndPlayerTeamIdOrderByPlayerJerseyNumberAsc(
                match.getId(), team.getId());

        playerStatistics.forEach(this::saveOrDeletePlayerAnalysis);

        if (!hasTeamAnalysisData(teamStatistic, playerStatistics)) {
            teamAnalysisRepository.deleteByTeamStatisticId(teamStatistic.getId());
            return;
        }

        var analysis = teamAnalysisRepository.findByTeamStatisticId(teamStatistic.getId())
                .orElseGet(() -> {
                    var created = new TeamAnalysis();
                    created.setTeamStatistic(teamStatistic);
                    return created;
                });

        analysis.setMostEfficientPlayer(bestPlayer(playerStatistics, this::playerEfficiency));
        analysis.setLeastEfficientPlayer(worstPlayer(playerStatistics, this::playerEfficiency));
        analysis.setTopPointsPlayer(bestPlayer(playerStatistics, PlayerStatistic::getPoints));
        analysis.setTopErrorsPlayer(bestPlayer(playerStatistics, PlayerStatistic::getErrors));
        analysis.setTopBlocksPlayer(bestPlayer(playerStatistics, PlayerStatistic::getBlocks));
        analysis.setTopServesPlayer(bestPlayer(playerStatistics, PlayerStatistic::getServes));
        analysis.setTopAssistsPlayer(bestPlayer(playerStatistics, PlayerStatistic::getAssists));
        analysis.setTeamEfficiency(teamEfficiency(teamStatistic));
        analysis.setAttackIndex(percentage(
                teamStatistic.getPoints() + teamStatistic.getBlocks(),
                teamStatistic.getPoints() + teamStatistic.getBlocks() + teamStatistic.getErrors()));
        analysis.setServeIndex(percentage(
                teamStatistic.getServes(),
                teamStatistic.getServes() + teamStatistic.getErrors()));
        analysis.setBlockIndex(percentage(
                teamStatistic.getBlocks(),
                teamStatistic.getPoints() + teamStatistic.getBlocks() + teamStatistic.getErrors()));
        analysis.setDisciplineIndex(clamp(100 - teamStatistic.getErrors() * 5, 0, 100));

        teamAnalysisRepository.save(analysis);
    }

    private void saveOrDeletePlayerAnalysis(PlayerStatistic statistic) {
        if (!hasPlayerAnalysisData(statistic)) {
            playerAnalysisRepository.deleteByPlayerStatisticId(statistic.getId());
            return;
        }

        var analysis = playerAnalysisRepository.findByPlayerStatisticId(statistic.getId())
                .orElseGet(() -> {
                    var created = new PlayerAnalysis();
                    created.setPlayerStatistic(statistic);
                    return created;
                });

        var efficiency = playerEfficiency(statistic);
        analysis.setEfficiency(efficiency);
        analysis.setServeContribution(clamp(statistic.getServes() * 10 - statistic.getErrors() * 3, 0, 100));
        analysis.setOverallRating(clamp(efficiency * 5 + analysis.getServeContribution() / 2, 0, 100));
        playerAnalysisRepository.save(analysis);
    }

    private Map<Long, PlayerStatistic> playerStatisticMap(Match match, OpponentTeam team) {
        return playerStatisticRepository.findByMatchIdAndPlayerTeamIdOrderByPlayerJerseyNumberAsc(
                        match.getId(), team.getId())
                .stream()
                .collect(Collectors.toMap(statistic -> statistic.getPlayer().getId(), statistic -> statistic));
    }

    private void backfillStatisticsIfNeeded(Match match) {
        if (teamStatisticRepository.existsByMatchId(match.getId())) {
            return;
        }

        matchEventRepository.findByMatchIdOrderByEventTimeAsc(match.getId()).forEach(this::applyEvent);
    }

    private Integer getSetsWon(Match match, OpponentTeam team) {
        var result = match.getResult();

        if (result == null || !result.contains(":")) {
            return 0;
        }

        var parts = result.split(":");

        if (parts.length != 2) {
            return 0;
        }

        try {
            return match.getHomeTeam().getId().equals(team.getId())
                    ? Integer.parseInt(parts[0].trim())
                    : Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Integer adjust(Integer value, int delta) {
        return Math.max(0, (value == null ? 0 : value) + delta);
    }

    private boolean hasTeamAnalysisData(TeamStatistic statistic, List<PlayerStatistic> playerStatistics) {
        return statistic.getPoints() > 0
                || statistic.getErrors() > 0
                || statistic.getServes() > 0
                || statistic.getBlocks() > 0
                || statistic.getSubstitutions() > 0
                || playerStatistics.stream().anyMatch(this::hasPlayerAnalysisData);
    }

    private boolean hasPlayerAnalysisData(PlayerStatistic statistic) {
        return statistic.getPoints() > 0
                || statistic.getErrors() > 0
                || statistic.getBlocks() > 0
                || statistic.getServes() > 0
                || statistic.getAssists() > 0;
    }

    private Integer teamEfficiency(TeamStatistic statistic) {
        return statistic.getPoints() * 2
                + statistic.getBlocks() * 2
                + statistic.getServes()
                - statistic.getErrors() * 2;
    }

    private Integer playerEfficiency(PlayerStatistic statistic) {
        return statistic.getPoints() * 2
                + statistic.getBlocks() * 2
                + statistic.getAssists()
                + statistic.getServes()
                - statistic.getErrors() * 2;
    }

    private OpponentPlayer bestPlayer(List<PlayerStatistic> statistics, ToIntFunction<PlayerStatistic> metric) {
        return statistics.stream()
                .filter(this::hasPlayerAnalysisData)
                .max(Comparator.comparingInt(metric))
                .map(PlayerStatistic::getPlayer)
                .orElse(null);
    }

    private OpponentPlayer worstPlayer(List<PlayerStatistic> statistics, ToIntFunction<PlayerStatistic> metric) {
        return statistics.stream()
                .filter(this::hasPlayerAnalysisData)
                .min(Comparator.comparingInt(metric))
                .map(PlayerStatistic::getPlayer)
                .orElse(null);
    }

    private Integer percentage(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) {
            return 0;
        }

        return clamp((int) Math.round((numerator == null ? 0 : numerator) * 100.0 / denominator), 0, 100);
    }

    private Integer clamp(Integer value, Integer min, Integer max) {
        return Math.max(min, Math.min(max, value));
    }
}
