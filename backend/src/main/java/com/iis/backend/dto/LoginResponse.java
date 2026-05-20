package com.iis.backend.dto;

import com.iis.backend.model.Role;

public record LoginResponse(
        String token,
        Long id,
        String username,
        String firstName,
        String lastName,
        Role role) {
}
