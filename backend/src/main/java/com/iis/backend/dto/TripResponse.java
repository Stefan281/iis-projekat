package com.iis.backend.dto;

import com.iis.backend.enums.TripStatus;
import com.iis.backend.model.Accommodation;
import com.iis.backend.model.Transport;
import com.iis.backend.model.Trip;

import java.time.LocalDate;
import java.util.List;

public class TripResponse {

    private Long id;
    private String name;
    private String location;
    private String purpose;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private TripStatus status;
    private String razlogOdbijanja;
    private AccommodationDTO selectedSmestaj;
    private TransportDTO selectedTransport;

    public TripResponse() {
    }

    public TripResponse(Long id, String name, String location, String purpose,
                        LocalDate departureDate, LocalDate returnDate, TripStatus status,
                        String razlogOdbijanja,
                        AccommodationDTO selectedSmestaj,
                        TransportDTO selectedTransport) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.purpose = purpose;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.status = status;
        this.razlogOdbijanja = razlogOdbijanja;
        this.selectedSmestaj = selectedSmestaj;
        this.selectedTransport = selectedTransport;
    }

    public static TripResponse from(Trip trip) {
        AccommodationDTO smestaj = null;
        if (trip.getAccommodationOptions() != null) {
            for (Accommodation a : trip.getAccommodationOptions()) {
                if (a.isSelected()) {
                    smestaj = AccommodationDTO.from(a);
                    break;
                }
            }
        }
        TransportDTO transport = null;
        if (trip.getTransportOptions() != null) {
            for (Transport t : trip.getTransportOptions()) {
                if (t.isSelected()) {
                    transport = TransportDTO.from(t);
                    break;
                }
            }
        }
        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getLocation(),
                trip.getPurpose(),
                trip.getDepartureDate(),
                trip.getReturnDate(),
                trip.getStatus(),
                trip.getRazlogOdbijanja(),
                smestaj,
                transport
        );
    }

    public String getRazlogOdbijanja() { return razlogOdbijanja; }
    public void setRazlogOdbijanja(String razlogOdbijanja) { this.razlogOdbijanja = razlogOdbijanja; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public AccommodationDTO getSelectedSmestaj() { return selectedSmestaj; }
    public void setSelectedSmestaj(AccommodationDTO selectedSmestaj) { this.selectedSmestaj = selectedSmestaj; }

    public TransportDTO getSelectedTransport() { return selectedTransport; }
    public void setSelectedTransport(TransportDTO selectedTransport) { this.selectedTransport = selectedTransport; }
}
