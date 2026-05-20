package com.iis.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.iis.backend.enums.StatusPutovanja;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "putovanje")
public class Putovanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private String lokacija;

    @Column
    private String povod;

    @Column(name = "datum_polaska", nullable = false)
    private LocalDate datumPolaska;

    @Column(name = "datum_povratka")
    private LocalDate datumPovratka;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPutovanja status;

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("putovanje-smestaj")
    private List<Smestaj> smestajOpcije = new ArrayList<>();

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("putovanje-transport")
    private List<Transport> transportOpcije = new ArrayList<>();

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("putovanje-igrac")
    private List<IgracPutovanje> putnici = new ArrayList<>();

    public Putovanje() {
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

    public List<Smestaj> getSmestajOpcije() {
        return smestajOpcije;
    }

    public void setSmestajOpcije(List<Smestaj> smestajOpcije) {
        this.smestajOpcije = smestajOpcije;
    }

    public List<Transport> getTransportOpcije() {
        return transportOpcije;
    }

    public void setTransportOpcije(List<Transport> transportOpcije) {
        this.transportOpcije = transportOpcije;
    }

    public List<IgracPutovanje> getPutnici() {
        return putnici;
    }

    public void setPutnici(List<IgracPutovanje> putnici) {
        this.putnici = putnici;
    }
}
