package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
        @NotBlank @Pattern(regexp = "\\d{13,19}") String cardNumber,
        @NotBlank @Pattern(regexp = "\\d{4}") String expiryDate,
        @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "\\d{3}") String cvv) {
}
