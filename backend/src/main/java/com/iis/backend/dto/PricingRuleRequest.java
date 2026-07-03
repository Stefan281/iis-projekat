package com.iis.backend.dto;

import com.iis.backend.model.PricingRuleCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PricingRuleRequest(
        @NotBlank String name,
        String description,
        @NotNull PricingRuleCondition condition,
        @NotNull @Positive BigDecimal coefficient,
        boolean active,
        int priority) {
}
