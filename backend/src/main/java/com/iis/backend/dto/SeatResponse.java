package com.iis.backend.dto;

import com.iis.backend.model.SeatStatus;

public record SeatResponse(
        Long id,
        String rowLabel,
        Integer seatNumber,
        SeatStatus status,
        Long zoneId,
        String zoneName) {
}
