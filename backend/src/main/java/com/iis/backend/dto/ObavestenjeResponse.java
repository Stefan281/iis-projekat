package com.iis.backend.dto;

import com.iis.backend.model.Notification;

import java.time.LocalDateTime;

public class ObavestenjeResponse {

    private Long id;
    private String tekst;
    private LocalDateTime datum;
    private Long autorId;

    public ObavestenjeResponse() {
    }

    public ObavestenjeResponse(Long id, String tekst, LocalDateTime datum, Long autorId) {
        this.id = id;
        this.tekst = tekst;
        this.datum = datum;
        this.autorId = autorId;
    }

    public static ObavestenjeResponse from(Notification n) {
        return new ObavestenjeResponse(
                n.getId(),
                n.getText(),
                n.getCreatedAt(),
                n.getAuthor() != null ? n.getAuthor().getId() : null
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTekst() { return tekst; }
    public void setTekst(String tekst) { this.tekst = tekst; }
    public LocalDateTime getDatum() { return datum; }
    public void setDatum(LocalDateTime datum) { this.datum = datum; }
    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }
}
