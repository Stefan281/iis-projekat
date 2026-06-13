package com.iis.backend.dto;

import com.iis.backend.model.Accommodation;

import java.math.BigDecimal;

public record AccommodationDTO(
        Long id,
        Long putovanjeId,
        String ime,
        String adresa,
        BigDecimal cena,
        boolean izabran
) {
    public static AccommodationDTO from(Accommodation a) {
        return new AccommodationDTO(
                a.getId(),
                a.getTrip() != null ? a.getTrip().getId() : null,
                a.getName(),
                a.getAddress(),
                a.getPrice(),
                a.isSelected()
        );
    }
}
