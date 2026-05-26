package com.iis.backend.dto;

import com.iis.backend.enums.TripStatus;
import jakarta.validation.constraints.NotNull;

public class TripStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TripStatus status;

    private String rejectionReason;

    public TripStatusUpdateRequest() {
    }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
