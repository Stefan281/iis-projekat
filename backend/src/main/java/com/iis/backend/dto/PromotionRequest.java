package com.iis.backend.dto;

import com.iis.backend.model.PromotionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PromotionRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPercentage,
        boolean active,
        int minTickets,
        PromotionType promotionType,
        String promoCode) {
}
