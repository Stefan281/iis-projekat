package com.iis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "poruka")
public class Poruka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "posiljalac_id", nullable = false)
    private Korisnik posiljalac;

    @ManyToOne(optional = false)
    @JoinColumn(name = "primalac_id", nullable = false)
    private Korisnik primalac;

    @Column(nullable = false, length = 2000)
    private String tekst;

    @Column(name = "vreme_slanja", nullable = false)
    private LocalDateTime vremeSlanja = LocalDateTime.now();

    public Poruka() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Korisnik getPosiljalac() {
        return posiljalac;
    }

    public void setPosiljalac(Korisnik posiljalac) {
        this.posiljalac = posiljalac;
    }

    public Korisnik getPrimalac() {
        return primalac;
    }

    public void setPrimalac(Korisnik primalac) {
        this.primalac = primalac;
    }

    public String getTekst() {
        return tekst;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public LocalDateTime getVremeSlanja() {
        return vremeSlanja;
    }

    public void setVremeSlanja(LocalDateTime vremeSlanja) {
        this.vremeSlanja = vremeSlanja;
    }
}
