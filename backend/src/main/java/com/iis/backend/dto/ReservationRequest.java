package com.iis.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull Long matchId,
        @NotNull Long seatId) {
}
