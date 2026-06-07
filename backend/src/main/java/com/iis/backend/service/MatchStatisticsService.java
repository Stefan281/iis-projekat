package com.iis.backend.service;

import com.iis.backend.dto.ActivityRecommendationResponse;
import com.iis.backend.dto.MatchStatisticsResponse;
import com.iis.backend.dto.PlayerAnalysisResponse;
import com.iis.backend.dto.PlayerStatisticResponse;
import com.iis.backend.dto.TeamAnalysisResponse;
import com.iis.backend.dto.TeamStatisticResponse;
import com.iis.backend.model.ActivityRecommendation;
import com.iis.backend.model.EventType;
import com.iis.backend.model.Match;
import com.iis.backend.model.MatchEvent;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.PlayerAnalysis;
import com.iis.backend.model.PlayerStatistic;
import com.iis.backend.model.RecommendationPriority;
import com.iis.backend.model.RecommendationType;
import com.iis.backend.model.TeamAnalysis;
import com.iis.backend.model.TeamStatistic;
import com.iis.backend.model.TeamType;
import com.iis.backend.repository.ActivityRecommendationRepository;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.PlayerAnalysisRepository;
import com.iis.backend.repository.PlayerStatisticRepository;
import com.iis.backend.repository.TeamAnalysisRepository;
import com.iis.backend.repository.TeamStatisticRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
    private final ActivityRecommendationRepository activityRecommendationRepository;

    public MatchStatisticsService(
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            TeamStatisticRepository teamStatisticRepository,
            PlayerStatisticRepository playerStatisticRepository,
            TeamAnalysisRepository teamAnalysisRepository,
            PlayerAnalysisRepository playerAnalysisRepository,
            ActivityRecommendationRepository activityRecommendationRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.teamStatisticRepository = teamStatisticRepository;
        this.playerStatisticRepository = playerStatisticRepository;
        this.teamAnalysisRepository = teamAnalysisRepository;
        this.playerAnalysisRepository = playerAnalysisRepository;
        this.activityRecommendationRepository = activityRecommendationRepository;
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
                getPlayerAnalyses(match, match.getAwayTeam()),
                getRecommendations(match));
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

    private List<ActivityRecommendationResponse> getRecommendations(Match match) {
        return activityRecommendationRepository
                .findByTeamAnalysisTeamStatisticMatchId(match.getId())
                .stream()
                .sorted(Comparator
                        .comparingInt((ActivityRecommendation recommendation) -> priorityRank(recommendation.getPriority()))
                        .thenComparing(ActivityRecommendation::getCreatedAt))
                .map(ActivityRecommendationResponse::fromEntity)
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
            teamAnalysisRepository.findByTeamStatisticId(teamStatistic.getId())
                    .ifPresent(analysis -> activityRecommendationRepository.deleteByTeamAnalysisId(analysis.getId()));
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
        analysis.setAttackIndex(index10(
                teamStatistic.getPoints() + teamStatistic.getBlocks(),
                teamStatistic.getPoints() + teamStatistic.getBlocks() + teamStatistic.getErrors()));
        analysis.setServeIndex(index10(
                teamStatistic.getServes(),
                teamStatistic.getServes() + teamStatistic.getErrors()));
        analysis.setBlockIndex(index10(
                teamStatistic.getBlocks(),
                teamStatistic.getPoints() + teamStatistic.getBlocks() + teamStatistic.getErrors()));
        analysis.setDisciplineIndex(clamp(10 - teamStatistic.getErrors(), 0, 10));

        var savedAnalysis = teamAnalysisRepository.save(analysis);
        regenerateRecommendations(savedAnalysis, playerStatistics);
    }

    private void regenerateRecommendations(TeamAnalysis analysis, List<PlayerStatistic> playerStatistics) {
        activityRecommendationRepository.deleteByTeamAnalysisId(analysis.getId());

        var recommendations = new ArrayList<ActivityRecommendation>();
        var teamStatistic = analysis.getTeamStatistic();
        var team = teamStatistic.getTeam();
        var isHomeTeam = team.getTeamType() == TeamType.HOME;
        var playerStatisticMap = playerStatistics.stream()
                .collect(Collectors.toMap(statistic -> statistic.getPlayer().getId(), statistic -> statistic));

        if (analysis.getDisciplineIndex() <= 4) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.REDUCE_ERRORS,
                    RecommendationPriority.HIGH,
                    isHomeTeam ? "Smanjiti broj gresaka" : "Iskoristiti greske protivnika",
                    isHomeTeam
                            ? "Tim ima nizak indeks discipline. Fokusirati se na sigurniju igru i smanjenje neiznudjenih gresaka."
                            : team.getName() + " ima nizak indeks discipline. Vrsiti pritisak i terati protivnika na rizicne odluke.");
        }

        if (analysis.getServeIndex() <= 4) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.IMPROVE_SERVE,
                    RecommendationPriority.MEDIUM,
                    isHomeTeam ? "Poboljsati servis" : "Napasti protivnicki servis",
                    isHomeTeam
                            ? "Indeks servisa je nizak. Potrebno je stabilizovati servis i smanjiti rizik."
                            : team.getName() + " ima slabiji indeks servisa. Iskoristiti njihove nestabilne servis sekvence.");
        } else if (analysis.getServeIndex() >= 7) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.KEEP_SERVE_PRESSURE,
                    RecommendationPriority.LOW,
                    isHomeTeam ? "Nastaviti pritisak servisom" : "Pripremiti prijem za jak servis",
                    isHomeTeam
                            ? "Indeks servisa je visok. Zadrzati trenutni ritam i koristiti servis kao sredstvo pritiska."
                            : team.getName() + " ima visok indeks servisa. Posebno obratiti paznju na organizaciju prijema.");
        }

        if (analysis.getAttackIndex() <= 4) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.IMPROVE_ATTACK,
                    RecommendationPriority.MEDIUM,
                    isHomeTeam ? "Pojacati napad" : "Zadrzati pritisak na protivnicki napad",
                    isHomeTeam
                            ? "Indeks napada je nizak. Potrebno je traziti sigurnije i bolje rasporedjene napadacke opcije."
                            : team.getName() + " ima nizak indeks napada. Nastaviti sa pritiskom i zatvaranjem glavnih napadackih opcija.");
        }

        if (analysis.getBlockIndex() <= 3) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.IMPROVE_BLOCK,
                    RecommendationPriority.MEDIUM,
                    isHomeTeam ? "Pojacati blok igru" : "Napadati prostor oko bloka",
                    isHomeTeam
                            ? "Indeks blokova je nizak. Potrebna je bolja komunikacija i postavljanje u bloku."
                            : team.getName() + " ima slabiji indeks blokova. Traziti napade kroz prostor oko njihovog bloka.");
        }

        if (analysis.getTeamEfficiency() <= -3) {
            addRecommendation(
                    recommendations,
                    analysis,
                    null,
                    RecommendationType.CONSIDER_TACTICAL_CHANGE,
                    RecommendationPriority.HIGH,
                    isHomeTeam ? "Razmotriti promenu ritma" : "Pojacati pritisak na neefikasan tim",
                    isHomeTeam
                            ? "Efikasnost tima je negativna. Razmotriti izmenu ili promenu taktickog pristupa."
                            : team.getName() + " ima negativnu efikasnost. Zadrzati pritisak dok protivnik ne stabilizuje igru.");
        }

        addPlayerRecommendationRules(recommendations, analysis, playerStatisticMap, isHomeTeam);

        activityRecommendationRepository.saveAll(recommendations);
    }

    private void addPlayerRecommendationRules(
            List<ActivityRecommendation> recommendations,
            TeamAnalysis analysis,
            Map<Long, PlayerStatistic> playerStatisticMap,
            boolean isHomeTeam) {
        var topPointsStatistic = statisticFor(analysis.getTopPointsPlayer(), playerStatisticMap);
        if (topPointsStatistic != null && topPointsStatistic.getPoints() >= 5) {
            addRecommendation(
                    recommendations,
                    analysis,
                    topPointsStatistic.getPlayer(),
                    RecommendationType.USE_TOP_SCORER,
                    RecommendationPriority.MEDIUM,
                    isHomeTeam ? "Usmeriti napad preko najboljeg poentera" : "Zaustaviti najboljeg poentera protivnika",
                    isHomeTeam
                            ? playerName(topPointsStatistic) + " ima najveci broj poena u timu. Koristiti ga u kljucnim napadima."
                            : playerName(topPointsStatistic) + " je najopasniji poenter protivnika. Potrebno je ograniciti njegove napadacke opcije.");
        }

        var efficientStatistic = statisticFor(analysis.getMostEfficientPlayer(), playerStatisticMap);
        if (efficientStatistic != null && scaledEfficiency(playerEfficiency(efficientStatistic)) >= 7) {
            addRecommendation(
                    recommendations,
                    analysis,
                    efficientStatistic.getPlayer(),
                    RecommendationType.USE_EFFICIENT_PLAYER,
                    RecommendationPriority.MEDIUM,
                    isHomeTeam ? "Koristiti najefikasnijeg igraca" : "Neutralisati najefikasnijeg igraca protivnika",
                    isHomeTeam
                            ? playerName(efficientStatistic) + " ima visok indeks efikasnosti. Preporucuje se vise akcija preko njega."
                            : playerName(efficientStatistic) + " ima visok indeks efikasnosti. Fokusirati odbranu i blok na tog igraca.");
        }

        var errorStatistic = statisticFor(analysis.getTopErrorsPlayer(), playerStatisticMap);
        if (errorStatistic != null && errorStatistic.getErrors() >= 3) {
            addRecommendation(
                    recommendations,
                    analysis,
                    errorStatistic.getPlayer(),
                    RecommendationType.WATCH_ERROR_PRONE_PLAYER,
                    RecommendationPriority.HIGH,
                    isHomeTeam ? "Smanjiti rizicne akcije kod igraca" : "Napadati igraca sklonog greskama",
                    isHomeTeam
                            ? playerName(errorStatistic) + " ima najveci broj gresaka. Smanjiti rizicne lopte i stabilizovati njegovu ulogu."
                            : playerName(errorStatistic) + " ima najveci broj gresaka kod protivnika. Usmeriti pritisak ka njemu.");
        }
    }

    private PlayerStatistic statisticFor(OpponentPlayer player, Map<Long, PlayerStatistic> playerStatisticMap) {
        if (player == null) {
            return null;
        }

        return playerStatisticMap.get(player.getId());
    }

    private String playerName(PlayerStatistic statistic) {
        return "#" + statistic.getPlayer().getJerseyNumber() + " " + statistic.getPlayer().getFullName();
    }

    private void addRecommendation(
            List<ActivityRecommendation> recommendations,
            TeamAnalysis analysis,
            OpponentPlayer player,
            RecommendationType type,
            RecommendationPriority priority,
            String title,
            String description) {
        var recommendation = new ActivityRecommendation();
        recommendation.setTeamAnalysis(analysis);
        recommendation.setPlayer(player);
        recommendation.setType(type);
        recommendation.setPriority(priority);
        recommendation.setTitle(title);
        recommendation.setDescription(description);
        recommendations.add(recommendation);
    }

    private Integer priorityRank(RecommendationPriority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
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
        analysis.setEfficiency(scaledEfficiency(efficiency));
        analysis.setServeContribution(clamp(statistic.getServes() * 2 - statistic.getErrors(), 0, 10));
        analysis.setOverallRating(overallRating(analysis.getEfficiency(), analysis.getServeContribution()));
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
        var positiveActions = statistic.getPoints() + statistic.getBlocks() + statistic.getServes();
        var totalActions = positiveActions + statistic.getErrors();

        if (totalActions == 0) {
            return 0;
        }

        return clamp((int) Math.round((positiveActions - statistic.getErrors()) * 10.0 / totalActions), -10, 10);
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

    private Integer index10(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) {
            return 0;
        }

        return clamp((int) Math.round((numerator == null ? 0 : numerator) * 10.0 / denominator), 0, 10);
    }

    private Integer scaledEfficiency(Integer rawEfficiency) {
        return clamp((int) Math.round((rawEfficiency == null ? 0 : rawEfficiency) / 2.0), -10, 10);
    }

    private Integer overallRating(Integer efficiency, Integer serveContribution) {
        var positiveEfficiency = Math.max(0, efficiency == null ? 0 : efficiency);
        var serveScore = serveContribution == null ? 0 : serveContribution;
        return clamp((int) Math.round(positiveEfficiency * 0.8 + serveScore * 0.2), 0, 10);
    }

    private Integer clamp(Integer value, Integer min, Integer max) {
        return Math.max(min, Math.min(max, value));
    }
}
