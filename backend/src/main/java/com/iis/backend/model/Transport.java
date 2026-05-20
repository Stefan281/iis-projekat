package com.iis.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "transport")
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "naziv_prevoznika", nullable = false)
    private String nazivPrevoznika;

    @Column(name = "vrsta_prevoza", nullable = false)
    private String vrstaPrevoza;

    @Column(nullable = false)
    private BigDecimal cena;

    @Column(nullable = false)
    private boolean izabran = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "putovanje_id", nullable = false)
    @JsonBackReference("putovanje-transport")
    private Putovanje putovanje;

    public Transport() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNazivPrevoznika() {
        return nazivPrevoznika;
    }

    public void setNazivPrevoznika(String nazivPrevoznika) {
        this.nazivPrevoznika = nazivPrevoznika;
    }

    public String getVrstaPrevoza() {
        return vrstaPrevoza;
    }

    public void setVrstaPrevoza(String vrstaPrevoza) {
        this.vrstaPrevoza = vrstaPrevoza;
    }

    public BigDecimal getCena() {
        return cena;
    }

    public void setCena(BigDecimal cena) {
        this.cena = cena;
    }

    public boolean isIzabran() {
        return izabran;
    }

    public void setIzabran(boolean izabran) {
        this.izabran = izabran;
    }

    public Putovanje getPutovanje() {
        return putovanje;
    }

    public void setPutovanje(Putovanje putovanje) {
        this.putovanje = putovanje;
    }
}
