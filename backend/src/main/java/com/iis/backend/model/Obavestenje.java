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
@Table(name = "obavestenje")
public class Obavestenje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String tekst;

    @Column(name = "datum_kreiranja", nullable = false)
    private LocalDateTime datumKreiranja = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Korisnik autor;

    public Obavestenje() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTekst() {
        return tekst;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setDatumKreiranja(LocalDateTime datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }

    public Korisnik getAutor() {
        return autor;
    }

    public void setAutor(Korisnik autor) {
        this.autor = autor;
    }
}
