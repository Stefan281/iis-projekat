package com.iis.backend.dto;

import com.iis.backend.model.Match;
import java.time.LocalDate;
import java.util.List;

public record MatchResponse(
        Long id,
        LocalDate matchDate,
        String result,
        String status,
        OpponentTeamResponse homeTeam,
        OpponentTeamResponse awayTeam,
        List<MatchEventResponse> events) {

    public static MatchResponse fromEntity(Match match, List<MatchEventResponse> events) {
        return new MatchResponse(
                match.getId(),
                match.getMatchDate(),
                match.getResult(),
                match.getStatus().name(),
                OpponentTeamResponse.fromEntity(match.getHomeTeam()),
                OpponentTeamResponse.fromEntity(match.getAwayTeam()),
                events);
    }
}
