package com.iis.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.iis.backend.enums.DocumentationStatus;
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
@Table(name = "trip_participant")
public class TripParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonBackReference("trip-participant")
    private Trip trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Enumerated(EnumType.STRING)
    @Column(name = "documentation_status", nullable = false)
    private DocumentationStatus documentationStatus = DocumentationStatus.TO_CHECK;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public TripParticipant() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public DocumentationStatus getDocumentationStatus() {
        return documentationStatus;
    }

    public void setDocumentationStatus(DocumentationStatus documentationStatus) {
        this.documentationStatus = documentationStatus;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
