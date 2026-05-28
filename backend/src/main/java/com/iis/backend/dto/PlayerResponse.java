package com.iis.backend.dto;

public record PlayerResponse(
        Long id,
        String firstName,
        String lastName,
        Integer birthYear,
        Integer height,
        String position,
        String club,
        String note) {
}
