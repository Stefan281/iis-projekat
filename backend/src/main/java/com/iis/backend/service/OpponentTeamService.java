package com.iis.backend.service;

import com.iis.backend.dto.OpponentPlayerRequest;
import com.iis.backend.dto.OpponentTeamRequest;
import com.iis.backend.dto.OpponentTeamResponse;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.PlayerStatus;
import com.iis.backend.model.TeamType;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.OpponentTeamRepository;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OpponentTeamService {

    private final OpponentTeamRepository opponentTeamRepository;
    private final MatchEventRepository matchEventRepository;

    public OpponentTeamService(
            OpponentTeamRepository opponentTeamRepository,
            MatchEventRepository matchEventRepository) {
        this.opponentTeamRepository = opponentTeamRepository;
        this.matchEventRepository = matchEventRepository;
    }

    public List<OpponentTeamResponse> getAll() {
        return opponentTeamRepository.findAll().stream()
                .map(OpponentTeamResponse::fromEntity)
                .toList();
    }

    public OpponentTeamResponse getById(Long id) {
        return OpponentTeamResponse.fromEntity(findOpponent(id));
    }

    public OpponentTeamResponse create(OpponentTeamRequest request) {
        validateTeamNameIsUnique(request.name(), null);
        validatePlayerJerseyNumbers(request.players());

        var team = new OpponentTeam();
        applyRequest(team, request);

        return OpponentTeamResponse.fromEntity(opponentTeamRepository.save(team));
    }

    @Transactional
    public OpponentTeamResponse update(Long id, OpponentTeamRequest request) {
        var team = findOpponent(id);
        validateTeamNameIsUnique(request.name(), id);
        validatePlayerJerseyNumbers(request.players());
        applyRequest(team, request);

        return OpponentTeamResponse.fromEntity(opponentTeamRepository.save(team));
    }

    public void delete(Long id) {
        var team = findOpponent(id);

        if (team.getTeamType() == TeamType.HOME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nas tim ne moze biti obrisan.");
        }

        opponentTeamRepository.deleteById(id);
    }

    private OpponentTeam findOpponent(Long id) {
        return opponentTeamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protivnik nije pronadjen."));
    }

    private void validateTeamNameIsUnique(String name, Long currentTeamId) {
        var exists = currentTeamId == null
                ? opponentTeamRepository.existsByNameIgnoreCase(name)
                : opponentTeamRepository.existsByNameIgnoreCaseAndIdNot(name, currentTeamId);

        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vec postoji protivnicki tim sa tim nazivom.");
        }
    }

    private void validatePlayerJerseyNumbers(List<OpponentPlayerRequest> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        var usedNumbers = new HashSet<Integer>();

        for (OpponentPlayerRequest player : players) {
            if (!usedNumbers.add(player.jerseyNumber())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "U okviru istog tima ne mogu postojati dva igraca sa istim brojem.");
            }
        }
    }

    private void applyRequest(OpponentTeam team, OpponentTeamRequest request) {
        team.setName(request.name().trim());
        team.setWins(request.wins());
        team.setLosses(request.losses());
        team.setCity(request.city().trim());
        team.setCoach(request.coach().trim());
        team.setPlayStyle(normalizeOptionalText(request.playStyle(), ""));
        team.setNote(normalizeOptionalText(request.note()));
        team.setTeamType(resolveTeamType(team, request.teamType()));
        syncPlayers(team, request.players());
    }

    private void syncPlayers(OpponentTeam team, List<OpponentPlayerRequest> playerRequests) {
        if (playerRequests == null) {
            deleteEventsForRemovedPlayers(team, List.of());
            team.setPlayers(List.of());
            return;
        }

        deleteEventsForRemovedPlayers(team, playerRequests);

        var updatedPlayers = new ArrayList<OpponentPlayer>();

        for (OpponentPlayerRequest request : playerRequests) {
            var player = findExistingPlayer(team, request.id());
            applyPlayerRequest(player, request);
            updatedPlayers.add(player);
        }

        team.setPlayers(updatedPlayers);
    }

    private void deleteEventsForRemovedPlayers(OpponentTeam team, List<OpponentPlayerRequest> playerRequests) {
        var keptPlayerIds = playerRequests.stream()
                .map(OpponentPlayerRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        team.getPlayers().stream()
                .map(OpponentPlayer::getId)
                .filter(Objects::nonNull)
                .filter(playerId -> !keptPlayerIds.contains(playerId))
                .forEach(playerId -> matchEventRepository.deleteByPrimaryPlayerIdOrSecondaryPlayerId(playerId, playerId));
    }

    private OpponentPlayer findExistingPlayer(OpponentTeam team, Long playerId) {
        if (playerId == null) {
            return new OpponentPlayer();
        }

        return team.getPlayers().stream()
                .filter(player -> Objects.equals(player.getId(), playerId))
                .findFirst()
                .orElseGet(OpponentPlayer::new);
    }

    private void applyPlayerRequest(OpponentPlayer player, OpponentPlayerRequest request) {
        player.setFullName(request.fullName().trim());
        player.setJerseyNumber(request.jerseyNumber());
        player.setPosition(request.position().trim());
        player.setHeight(request.height());
        player.setAge(request.age());
        player.setPlayerStatus(parsePlayerStatus(request.playerStatus()));
    }

    private String normalizeOptionalText(String value) {
        return normalizeOptionalText(value, null);
    }

    private String normalizeOptionalText(String value, String emptyValue) {
        if (value == null || value.isBlank()) {
            return emptyValue;
        }

        return value.trim();
    }

    private PlayerStatus parsePlayerStatus(String playerStatus) {
        if (playerStatus == null || playerStatus.isBlank()) {
            return PlayerStatus.BENCH;
        }

        return PlayerStatus.valueOf(playerStatus);
    }

    private TeamType parseTeamType(String teamType, TeamType fallback) {
        if (teamType == null || teamType.isBlank()) {
            return fallback == null ? TeamType.OPPONENT : fallback;
        }

        return TeamType.valueOf(teamType);
    }

    private TeamType resolveTeamType(OpponentTeam team, String requestedTeamType) {
        if (team.getTeamType() == TeamType.HOME) {
            return TeamType.HOME;
        }

        return parseTeamType(requestedTeamType, team.getTeamType());
    }
}
