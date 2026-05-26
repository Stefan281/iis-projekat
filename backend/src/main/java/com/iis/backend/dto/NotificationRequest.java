package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NotificationRequest {

    @NotBlank(message = "Notification text is required")
    @Size(max = 2000, message = "Text can be at most 2000 characters")
    private String text;

    public NotificationRequest() {
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
