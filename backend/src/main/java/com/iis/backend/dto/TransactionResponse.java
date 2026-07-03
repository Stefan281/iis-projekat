package com.iis.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long customerId,
        String customerFullName,
        Long reservationId,
        BigDecimal amount,
        String cardLastFour,
        LocalDateTime createdAt) {
}
