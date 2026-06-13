package com.iis.backend.dto;

import com.iis.backend.model.User;

public record UserDTO(
        Long id,
        String ime,
        String prezime,
        String role,
        boolean dodat
) {
    public static UserDTO from(User u) {
        return new UserDTO(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole() != null ? u.getRole().name() : null,
                false
        );
    }
}
