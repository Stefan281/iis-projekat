package com.iis.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomAssignRequest {

    @NotBlank
    private String roomNumber;

    public RoomAssignRequest() {}

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
