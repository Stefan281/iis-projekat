package com.iis.backend.dto;

import com.iis.backend.enums.DocumentationStatus;
import com.iis.backend.model.TripParticipant;
import com.iis.backend.model.User;

public record PassengerRowDTO(
        Long id,
        Long userId,
        Long participantId,
        String firstName,
        String lastName,
        String role,
        String roomNumber,
        DocumentationStatus documentationStatus,
        boolean added
) {
    public static PassengerRowDTO fromUser(User user) {
        return new PassengerRowDTO(
                user.getId(),
                user.getId(),
                null,
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                null,
                DocumentationStatus.TO_CHECK,
                false
        );
    }

    public static PassengerRowDTO fromParticipant(TripParticipant participant) {
        User user = participant.getPlayer();
        return new PassengerRowDTO(
                user != null ? user.getId() : null,
                user != null ? user.getId() : null,
                participant.getId(),
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName() : null,
                user != null && user.getRole() != null ? user.getRole().name() : null,
                participant.getRoomNumber(),
                participant.getDocumentationStatus(),
                participant.isChecked()
        );
    }
}
