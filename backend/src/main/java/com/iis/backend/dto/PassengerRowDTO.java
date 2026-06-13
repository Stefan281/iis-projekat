package com.iis.backend.dto;

import com.iis.backend.enums.DocumentationStatus;
import com.iis.backend.model.TripParticipant;
import com.iis.backend.model.User;

/**
 * One row of the passengers table: a team member merged with their
 * {@link TripParticipant} data for a given trip (if it exists).
 *
 * {@code id} mirrors {@code userId} because the frontend keys rows by user.
 * {@code participantId} is {@code null} when the member has no participant row yet.
 */
public record PassengerRowDTO(
        Long id,
        Long userId,
        Long participantId,
        String ime,
        String prezime,
        String role,
        String sobaBroj,
        DocumentationStatus dokumentacijaStatus,
        boolean added
) {
    /** Build a row for a team member who has no participant record for this trip. */
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

    /** Build a row for a team member who has a participant record for this trip. */
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
