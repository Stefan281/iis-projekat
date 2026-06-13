package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class AccommodationCreateRequest {

    @NotBlank
    private String ime;

    @NotBlank
    private String adresa;

    @NotNull
    @PositiveOrZero
    private BigDecimal cena;

    public AccommodationCreateRequest() {}

    public String getIme() { return ime; }
    public void setIme(String ime) { this.ime = ime; }
    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }
    public BigDecimal getCena() { return cena; }
    public void setCena(BigDecimal cena) { this.cena = cena; }
}
