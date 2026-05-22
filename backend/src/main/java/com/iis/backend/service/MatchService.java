package com.iis.backend.service;

import com.iis.backend.dto.MatchEventRequest;
import com.iis.backend.dto.MatchEventResponse;
import com.iis.backend.dto.MatchResponse;
import com.iis.backend.model.MatchEvent;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.OpponentPlayerRepository;
import com.iis.backend.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final OpponentPlayerRepository opponentPlayerRepository;
    private final UserRepository userRepository;

    public MatchService(
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            OpponentPlayerRepository opponentPlayerRepository,
            UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.opponentPlayerRepository = opponentPlayerRepository;
        this.userRepository = userRepository;
    }

    public MatchResponse getCurrentMatch() {
        var match = matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema aktivne utakmice."));

        return MatchResponse.fromEntity(match, getRecentEvents(match.getId()));
    }

    public MatchEventResponse addEvent(Long matchId, MatchEventRequest request) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utakmica nije pronadjena."));
        var statistician = userRepository.findById(request.statisticianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Statisticar nije pronadjen."));
        var primaryPlayer = opponentPlayerRepository.findById(request.primaryPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen."));
        var secondaryPlayer = request.secondaryPlayerId() == null
                ? null
                : opponentPlayerRepository.findById(request.secondaryPlayerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac za izmenu nije pronadjen."));

        if (!belongsToMatch(primaryPlayer.getTeam().getId(), match)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Igrac ne pripada timovima na ovoj utakmici.");
        }

        if (secondaryPlayer != null && !belongsToMatch(secondaryPlayer.getTeam().getId(), match)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Igrac za izmenu ne pripada timovima na ovoj utakmici.");
        }

        var event = new MatchEvent();
        event.setMatch(match);
        event.setStatistician(statistician);
        event.setPrimaryPlayer(primaryPlayer);
        event.setSecondaryPlayer(secondaryPlayer);
        event.setEventType(request.eventType());
        event.setDescription(normalizeDescription(request.description()));
        event.setEventTime(LocalDateTime.now());

        return MatchEventResponse.fromEntity(matchEventRepository.save(event));
    }

    private java.util.List<MatchEventResponse> getRecentEvents(Long matchId) {
        return matchEventRepository.findTop10ByMatchIdOrderByEventTimeDesc(matchId).stream()
                .map(MatchEventResponse::fromEntity)
                .toList();
    }

    private boolean belongsToMatch(Long teamId, com.iis.backend.model.Match match) {
        return match.getHomeTeam().getId().equals(teamId) || match.getAwayTeam().getId().equals(teamId);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
