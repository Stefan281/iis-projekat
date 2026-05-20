package com.iis.backend.dto;

import com.iis.backend.enums.StatusPutovanja;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PutovanjeRequest {

    @NotBlank(message = "Naziv putovanja je obavezan")
    @Size(max = 150, message = "Naziv putovanja može imati najviše 150 karaktera")
    private String naziv;

    @NotBlank(message = "Lokacija je obavezna")
    @Size(max = 150, message = "Lokacija može imati najviše 150 karaktera")
    private String lokacija;

    @Size(max = 500, message = "Povod može imati najviše 500 karaktera")
    private String povod;

    @NotNull(message = "Datum polaska je obavezan")
    @FutureOrPresent(message = "Datum polaska mora biti danas ili u budućnosti")
    private LocalDate datumPolaska;

    private LocalDate datumPovratka;

    @NotNull(message = "Status putovanja je obavezan")
    private StatusPutovanja status;

    public PutovanjeRequest() {
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getLokacija() {
        return lokacija;
    }

    public void setLokacija(String lokacija) {
        this.lokacija = lokacija;
    }

    public String getPovod() {
        return povod;
    }

    public void setPovod(String povod) {
        this.povod = povod;
    }

    public LocalDate getDatumPolaska() {
        return datumPolaska;
    }

    public void setDatumPolaska(LocalDate datumPolaska) {
        this.datumPolaska = datumPolaska;
    }

    public LocalDate getDatumPovratka() {
        return datumPovratka;
    }

    public void setDatumPovratka(LocalDate datumPovratka) {
        this.datumPovratka = datumPovratka;
    }

    public StatusPutovanja getStatus() {
        return status;
    }

    public void setStatus(StatusPutovanja status) {
        this.status = status;
    }
}
