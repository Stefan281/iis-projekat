package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ObavestenjeRequest {

    @NotBlank(message = "Tekst obavestenja je obavezan")
    @Size(max = 2000, message = "Tekst moze biti najvise 2000 karaktera")
    private String tekst;

    public ObavestenjeRequest() {
    }

    public String getTekst() { return tekst; }
    public void setTekst(String tekst) { this.tekst = tekst; }
}
