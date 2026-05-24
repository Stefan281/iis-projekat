package com.iis.backend.service;

import com.iis.backend.dto.MatchStatisticsResponse;
import com.iis.backend.dto.PlayerStatisticResponse;
import com.iis.backend.dto.TeamStatisticResponse;
import com.iis.backend.model.EventType;
import com.iis.backend.model.Match;
import com.iis.backend.model.MatchEvent;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.PlayerStatistic;
import com.iis.backend.model.TeamStatistic;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.PlayerStatisticRepository;
import com.iis.backend.repository.TeamStatisticRepository;
import java.util.List;
import java.util.Comparator;
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

    public MatchStatisticsService(
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            TeamStatisticRepository teamStatisticRepository,
            PlayerStatisticRepository playerStatisticRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.teamStatisticRepository = teamStatisticRepository;
        this.playerStatisticRepository = playerStatisticRepository;
    }

    @Transactional
    public MatchStatisticsResponse getCurrentMatchStatistics() {
        var match = matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema aktivne utakmice."));

        backfillStatisticsIfNeeded(match);

        var homeTeamStatistic = getOrCreateTeamStatistic(match, match.getHomeTeam());
        var awayTeamStatistic = getOrCreateTeamStatistic(match, match.getAwayTeam());

        return new MatchStatisticsResponse(
                match.getId(),
                match.getMatchDate(),
                match.getResult(),
                match.getStatus().name(),
                TeamStatisticResponse.fromEntity(homeTeamStatistic),
                TeamStatisticResponse.fromEntity(awayTeamStatistic),
                getPlayerStatistics(match, match.getHomeTeam()),
                getPlayerStatistics(match, match.getAwayTeam()));
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
}
