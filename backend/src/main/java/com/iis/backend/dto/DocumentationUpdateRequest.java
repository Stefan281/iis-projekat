package com.iis.backend.dto;

import com.iis.backend.enums.DocumentationStatus;
import jakarta.validation.constraints.NotNull;

public class DocumentationUpdateRequest {

    @NotNull
    private DocumentationStatus status;

    public DocumentationUpdateRequest() {}

    public DocumentationStatus getStatus() { return status; }
    public void setStatus(DocumentationStatus status) { this.status = status; }
}
