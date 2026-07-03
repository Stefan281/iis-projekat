package com.iis.backend.dto;

import java.math.BigDecimal;

public record PriceTotalResponse(
        BigDecimal baseTotal,
        BigDecimal discount,
        BigDecimal finalTotal,
        String promotionName) {
}
