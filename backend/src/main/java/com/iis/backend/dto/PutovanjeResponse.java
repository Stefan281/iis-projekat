package com.iis.backend.dto;

import com.iis.backend.enums.StatusPutovanja;
import com.iis.backend.model.Putovanje;

import java.time.LocalDate;

public class PutovanjeResponse {

    private Long id;
    private String naziv;
    private String lokacija;
    private String povod;
    private LocalDate datumPolaska;
    private LocalDate datumPovratka;
    private StatusPutovanja status;

    public PutovanjeResponse() {
    }

    public PutovanjeResponse(Long id, String naziv, String lokacija, String povod,
                             LocalDate datumPolaska, LocalDate datumPovratka, StatusPutovanja status) {
        this.id = id;
        this.naziv = naziv;
        this.lokacija = lokacija;
        this.povod = povod;
        this.datumPolaska = datumPolaska;
        this.datumPovratka = datumPovratka;
        this.status = status;
    }

    public static PutovanjeResponse from(Putovanje putovanje) {
        return new PutovanjeResponse(
                putovanje.getId(),
                putovanje.getNaziv(),
                putovanje.getLokacija(),
                putovanje.getPovod(),
                putovanje.getDatumPolaska(),
                putovanje.getDatumPovratka(),
                putovanje.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
