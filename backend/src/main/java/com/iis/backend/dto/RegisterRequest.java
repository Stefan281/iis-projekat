package com.iis.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String street,
        @NotBlank String streetNumber,
        @NotBlank String postalCode,
        @NotBlank String phoneNumber,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String confirmPassword) {
}
