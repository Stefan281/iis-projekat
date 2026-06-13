package com.iis.backend.dto;

import com.iis.backend.model.Transport;

import java.math.BigDecimal;

public record TransportDTO(
        Long id,
        Long putovanjeId,
        String naziv,
        String vrsta,
        BigDecimal cena,
        boolean izabran
) {
    public static TransportDTO from(Transport t) {
        return new TransportDTO(
                t.getId(),
                t.getTrip() != null ? t.getTrip().getId() : null,
                t.getCarrierName(),
                t.getTransportType(),
                t.getPrice(),
                t.isSelected()
        );
    }
}
