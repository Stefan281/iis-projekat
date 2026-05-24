package com.iis.backend.service;

import com.iis.backend.dto.MatchEventRequest;
import com.iis.backend.dto.MatchEventResponse;
import com.iis.backend.dto.MatchResponse;
import com.iis.backend.model.MatchEvent;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.EventType;
import com.iis.backend.model.PlayerStatus;
import com.iis.backend.repository.MatchEventRepository;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.OpponentPlayerRepository;
import com.iis.backend.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final OpponentPlayerRepository opponentPlayerRepository;
    private final UserRepository userRepository;
    private final MatchStatisticsService matchStatisticsService;

    public MatchService(
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            OpponentPlayerRepository opponentPlayerRepository,
            UserRepository userRepository,
            MatchStatisticsService matchStatisticsService) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.opponentPlayerRepository = opponentPlayerRepository;
        this.userRepository = userRepository;
        this.matchStatisticsService = matchStatisticsService;
    }

    public MatchResponse getCurrentMatch() {
        var match = matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema aktivne utakmice."));

        return MatchResponse.fromEntity(match, getRecentEvents(match.getId()));
    }

    @Transactional
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

        validateEventPlayers(request, primaryPlayer, secondaryPlayer);

        var event = new MatchEvent();
        event.setMatch(match);
        event.setStatistician(statistician);
        event.setPrimaryPlayer(primaryPlayer);
        event.setSecondaryPlayer(secondaryPlayer);
        event.setEventType(request.eventType());
        event.setEventTime(LocalDateTime.now());

        if (request.eventType() == EventType.SUBSTITUTION) {
            primaryPlayer.setPlayerStatus(PlayerStatus.BENCH);
            secondaryPlayer.setPlayerStatus(PlayerStatus.IN_GAME);
            opponentPlayerRepository.save(primaryPlayer);
            opponentPlayerRepository.save(secondaryPlayer);
        }

        var savedEvent = matchEventRepository.save(event);
        matchStatisticsService.applyEvent(savedEvent);

        return MatchEventResponse.fromEntity(savedEvent);
    }

    @Transactional
    public void deleteEvent(Long matchId, Long eventId) {
        var event = matchEventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dogadjaj nije pronadjen."));

        if (!event.getMatch().getId().equals(matchId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dogadjaj ne pripada izabranoj utakmici.");
        }

        if (event.getEventType() == EventType.SUBSTITUTION) {
            event.getPrimaryPlayer().setPlayerStatus(PlayerStatus.IN_GAME);
            event.getSecondaryPlayer().setPlayerStatus(PlayerStatus.BENCH);
            opponentPlayerRepository.save(event.getPrimaryPlayer());
            opponentPlayerRepository.save(event.getSecondaryPlayer());
        }

        matchStatisticsService.revertEvent(event);
        matchEventRepository.delete(event);
    }

    private java.util.List<MatchEventResponse> getRecentEvents(Long matchId) {
        return matchEventRepository.findTop10ByMatchIdOrderByEventTimeDesc(matchId).stream()
                .map(MatchEventResponse::fromEntity)
                .toList();
    }

    private boolean belongsToMatch(Long teamId, com.iis.backend.model.Match match) {
        return match.getHomeTeam().getId().equals(teamId) || match.getAwayTeam().getId().equals(teamId);
    }

    private void validateEventPlayers(
            MatchEventRequest request,
            com.iis.backend.model.OpponentPlayer primaryPlayer,
            com.iis.backend.model.OpponentPlayer secondaryPlayer) {
        if (request.eventType() == EventType.SUBSTITUTION) {
            if (secondaryPlayer == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izaberite igraca koji ulazi.");
            }

            if (!primaryPlayer.getTeam().getId().equals(secondaryPlayer.getTeam().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izmena mora biti u okviru istog tima.");
            }

            if (primaryPlayer.getPlayerStatus() != PlayerStatus.IN_GAME) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Igrac koji izlazi mora biti u igri.");
            }

            if (secondaryPlayer.getPlayerStatus() != PlayerStatus.BENCH) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Igrac koji ulazi mora biti na klupi.");
            }

            return;
        }

        if (secondaryPlayer != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drugi igrac se koristi samo za izmenu.");
        }

        if (primaryPlayer.getPlayerStatus() != PlayerStatus.IN_GAME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dogadjaj se moze uneti samo za igraca koji je u igri.");
        }
    }
}
