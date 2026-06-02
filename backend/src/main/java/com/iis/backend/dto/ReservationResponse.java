package com.iis.backend.dto;

import com.iis.backend.model.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        Long customerId,
        String customerFullName,
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
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {
}
