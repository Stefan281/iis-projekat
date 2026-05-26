package com.iis.backend.dto;

import com.iis.backend.enums.TripStatus;
import com.iis.backend.model.Trip;

import java.time.LocalDate;

public class TripResponse {

    private Long id;
    private String name;
    private String location;
    private String purpose;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private TripStatus status;
    private String rejectionReason;

    public TripResponse() {
    }

    public TripResponse(Long id, String name, String location, String purpose,
                        LocalDate departureDate, LocalDate returnDate, TripStatus status,
                        String rejectionReason) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.purpose = purpose;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getLocation(),
                trip.getPurpose(),
                trip.getDepartureDate(),
                trip.getReturnDate(),
                trip.getStatus(),
                trip.getRejectionReason()
        );
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
