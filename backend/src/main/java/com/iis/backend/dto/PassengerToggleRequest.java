package com.iis.backend.dto;

import jakarta.validation.constraints.NotNull;

public class PassengerToggleRequest {

    @NotNull
    private Long userId;

    private boolean checked;

    public PassengerToggleRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
