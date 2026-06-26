package com.iis.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseRequest(
        @NotNull Long matchId,
        Long seatId,
        List<Long> seatIds) {
}
