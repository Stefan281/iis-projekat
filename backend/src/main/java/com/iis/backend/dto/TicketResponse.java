package com.iis.backend.dto;

import com.iis.backend.model.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TicketResponse(
        Long id,
        Long matchId,
        String homeTeam,
        String awayTeam,
        LocalDate matchDate,
        LocalTime matchTime,
        String location,
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        Long zoneId,
        String zoneName,
        BigDecimal price,
        TicketStatus status,
        LocalDateTime purchasedAt) {
}
