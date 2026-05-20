package com.iis.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "smestaj")
public class Smestaj {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private String adresa;

    @Column(nullable = false)
    private BigDecimal cena;

    @Column(nullable = false)
    private boolean izabran = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "putovanje_id", nullable = false)
    @JsonBackReference("putovanje-smestaj")
    private Putovanje putovanje;

    @OneToMany(mappedBy = "smestaj", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("smestaj-soba")
    private List<Soba> sobe = new ArrayList<>();

    public Smestaj() {
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

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
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

    public List<Soba> getSobe() {
        return sobe;
    }

    public void setSobe(List<Soba> sobe) {
        this.sobe = sobe;
    }
}
