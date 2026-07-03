package com.iis.backend.dto;

import java.math.BigDecimal;

public record MatchSalesStats(
        Long matchId,
        String homeTeam,
        String awayTeam,
        String matchDate,
        long soldTickets,
        long activeReservations,
        long availableSeats,
        BigDecimal revenue) {
}
