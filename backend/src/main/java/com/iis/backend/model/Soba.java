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

@Entity
@Table(name = "soba")
public class Soba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broj_sobe", nullable = false)
    private String brojSobe;

    @Column(nullable = false)
    private Integer kapacitet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "smestaj_id", nullable = false)
    @JsonBackReference("smestaj-soba")
    private Smestaj smestaj;

    public Soba() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrojSobe() {
        return brojSobe;
    }

    public void setBrojSobe(String brojSobe) {
        this.brojSobe = brojSobe;
    }

    public Integer getKapacitet() {
        return kapacitet;
    }

    public void setKapacitet(Integer kapacitet) {
        this.kapacitet = kapacitet;
    }

    public Smestaj getSmestaj() {
        return smestaj;
    }

    public void setSmestaj(Smestaj smestaj) {
        this.smestaj = smestaj;
    }
}
