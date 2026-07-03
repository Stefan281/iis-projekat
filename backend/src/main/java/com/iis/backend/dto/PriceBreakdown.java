package com.iis.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PriceBreakdown(
        BigDecimal basePrice,
        BigDecimal zoneCoefficient,
        String zoneName,
        BigDecimal ticketTypeCoefficient,
        String ticketTypeName,
        List<AppliedRule> appliedRules,
        BigDecimal promotionDiscount,
        String promotionName,
        BigDecimal finalPrice) {

    public record AppliedRule(String name, BigDecimal coefficient) {}
}
