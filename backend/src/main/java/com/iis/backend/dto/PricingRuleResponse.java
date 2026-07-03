package com.iis.backend.dto;

import com.iis.backend.model.PricingRuleCondition;
import java.math.BigDecimal;

public record PricingRuleResponse(
        Long id,
        String name,
        String description,
        PricingRuleCondition condition,
        BigDecimal coefficient,
        boolean active,
        int priority) {
}
