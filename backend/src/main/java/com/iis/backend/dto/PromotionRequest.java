package com.iis.backend.dto;

import com.iis.backend.model.PromotionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.0") BigDecimal discountPercentage,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull PromotionStatus status) {
}
