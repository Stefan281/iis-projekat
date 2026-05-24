package com.iis.backend.dto;

import com.iis.backend.enums.TripStatus;
import jakarta.validation.constraints.NotNull;

public class TripStatusUpdateRequest {

    @NotNull(message = "Status je obavezan")
    private TripStatus status;

    private String razlogOdbijanja;

    public TripStatusUpdateRequest() {
    }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public String getRazlogOdbijanja() { return razlogOdbijanja; }
    public void setRazlogOdbijanja(String razlogOdbijanja) { this.razlogOdbijanja = razlogOdbijanja; }
}
