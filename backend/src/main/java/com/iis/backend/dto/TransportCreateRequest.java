package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class TransportCreateRequest {

    @NotBlank
    private String naziv;

    @NotBlank
    private String vrsta;

    @NotNull
    @PositiveOrZero
    private BigDecimal cena;

    public TransportCreateRequest() {}

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public String getVrsta() { return vrsta; }
    public void setVrsta(String vrsta) { this.vrsta = vrsta; }
    public BigDecimal getCena() { return cena; }
    public void setCena(BigDecimal cena) { this.cena = cena; }
}
