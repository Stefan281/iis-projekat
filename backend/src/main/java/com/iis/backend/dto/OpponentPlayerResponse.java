package com.iis.backend.dto;

import com.iis.backend.model.OpponentPlayer;

public record OpponentPlayerResponse(
        Long id,
        String fullName,
        Integer jerseyNumber,
        String position,
        Integer height,
        Integer age) {

    public static OpponentPlayerResponse fromEntity(OpponentPlayer player) {
        return new OpponentPlayerResponse(
                player.getId(),
                player.getFullName(),
                player.getJerseyNumber(),
                player.getPosition(),
                player.getHeight(),
                player.getAge());
    }
}
