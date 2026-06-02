package com.iis.backend.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        @NotNull Long matchId,
        @NotNull Long seatId) {
}
