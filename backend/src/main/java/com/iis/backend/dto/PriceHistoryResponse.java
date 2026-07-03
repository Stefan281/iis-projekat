package com.iis.backend.dto;

import com.iis.backend.model.PriceHistorySource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryResponse(
        Long id,
        Long pricingRuleId,
        String pricingRuleName,
        BigDecimal oldCoefficient,
        BigDecimal newCoefficient,
        String description,
        PriceHistorySource source,
        LocalDateTime changedAt) {
}
