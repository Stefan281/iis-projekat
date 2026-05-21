package com.iis.backend.service;

import com.iis.backend.dto.OpponentPlayerRequest;
import com.iis.backend.dto.OpponentTeamRequest;
import com.iis.backend.dto.OpponentTeamResponse;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.repository.OpponentTeamRepository;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OpponentTeamService {

    private final OpponentTeamRepository opponentTeamRepository;

    public OpponentTeamService(OpponentTeamRepository opponentTeamRepository) {
        this.opponentTeamRepository = opponentTeamRepository;
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

    public OpponentTeamResponse update(Long id, OpponentTeamRequest request) {
        var team = findOpponent(id);
        validateTeamNameIsUnique(request.name(), id);
        validatePlayerJerseyNumbers(request.players());
        applyRequest(team, request);

        return OpponentTeamResponse.fromEntity(opponentTeamRepository.save(team));
    }

    public void delete(Long id) {
        if (!opponentTeamRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Protivnik nije pronadjen.");
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
        team.setPlayers(toPlayers(request.players()));
    }

    private List<OpponentPlayer> toPlayers(List<OpponentPlayerRequest> playerRequests) {
        if (playerRequests == null) {
            return List.of();
        }

        return playerRequests.stream()
                .map(this::toPlayer)
                .toList();
    }

    private OpponentPlayer toPlayer(OpponentPlayerRequest request) {
        var player = new OpponentPlayer();
        player.setFullName(request.fullName().trim());
        player.setJerseyNumber(request.jerseyNumber());
        player.setPosition(request.position().trim());
        player.setHeight(request.height());
        player.setAge(request.age());
        return player;
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
}
