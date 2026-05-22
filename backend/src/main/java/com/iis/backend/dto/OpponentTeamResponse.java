package com.iis.backend.dto;

import com.iis.backend.model.OpponentTeam;
import java.util.List;

public record OpponentTeamResponse(
        Long id,
        String name,
        Integer wins,
        Integer losses,
        String city,
        String coach,
        String playStyle,
        String note,
        String teamType,
        List<OpponentPlayerResponse> players) {

    public static OpponentTeamResponse fromEntity(OpponentTeam team) {
        return new OpponentTeamResponse(
                team.getId(),
                team.getName(),
                team.getWins(),
                team.getLosses(),
                team.getCity(),
                team.getCoach(),
                team.getPlayStyle(),
                team.getNote(),
                team.getTeamType() == null ? "OPPONENT" : team.getTeamType().name(),
                team.getPlayers().stream()
                        .map(OpponentPlayerResponse::fromEntity)
                        .toList());
    }
}
