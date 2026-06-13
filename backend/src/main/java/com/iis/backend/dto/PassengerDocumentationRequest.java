package com.iis.backend.dto;

import com.iis.backend.enums.DocumentationStatus;
import jakarta.validation.constraints.NotNull;

/** Sets the documentation status for a team member who is not yet a participant of the trip. */
public class PassengerDocumentationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private DocumentationStatus status;

    public PassengerDocumentationRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public DocumentationStatus getStatus() { return status; }
    public void setStatus(DocumentationStatus status) { this.status = status; }
}
