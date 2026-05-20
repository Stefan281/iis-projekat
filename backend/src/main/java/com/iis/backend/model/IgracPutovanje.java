package com.iis.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.iis.backend.enums.StatusDokumentacije;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "igrac_putovanje")
public class IgracPutovanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "putovanje_id", nullable = false)
    @JsonBackReference("putovanje-igrac")
    private Putovanje putovanje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "igrac_id", nullable = false)
    private Korisnik igrac;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_dokumentacije", nullable = false)
    private StatusDokumentacije statusDokumentacije = StatusDokumentacije.PROVERITI;

    @ManyToOne
    @JoinColumn(name = "soba_id")
    private Soba soba;

    public IgracPutovanje() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Putovanje getPutovanje() {
        return putovanje;
    }

    public void setPutovanje(Putovanje putovanje) {
        this.putovanje = putovanje;
    }

    public Korisnik getIgrac() {
        return igrac;
    }

    public void setIgrac(Korisnik igrac) {
        this.igrac = igrac;
    }

    public StatusDokumentacije getStatusDokumentacije() {
        return statusDokumentacije;
    }

    public void setStatusDokumentacije(StatusDokumentacije statusDokumentacije) {
        this.statusDokumentacije = statusDokumentacije;
    }

    public Soba getSoba() {
        return soba;
    }

    public void setSoba(Soba soba) {
        this.soba = soba;
    }
}
