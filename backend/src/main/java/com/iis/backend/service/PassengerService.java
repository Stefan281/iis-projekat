package com.iis.backend.service;

import com.iis.backend.dto.DocumentationUpdateRequest;
import com.iis.backend.dto.PassengerDocumentationRequest;
import com.iis.backend.dto.PassengerRowDTO;
import com.iis.backend.dto.PassengerToggleRequest;
import com.iis.backend.dto.RoomAssignRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Role;
import com.iis.backend.model.Trip;
import com.iis.backend.model.TripParticipant;
import com.iis.backend.model.User;
import com.iis.backend.repository.TripParticipantRepository;
import com.iis.backend.repository.TripRepository;
import com.iis.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PassengerService {

    /** Roles that are eligible to travel and therefore appear in the passengers table. */
    private static final List<Role> TEAM_ROLES =
            List.of(Role.IGRAC, Role.STRUCNI_STAB, Role.STATISTICAR);

    private final TripParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public PassengerService(TripParticipantRepository participantRepository,
                            TripRepository tripRepository,
                            UserRepository userRepository) {
        this.participantRepository = participantRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns one row per team member, merged with their participant data for this
     * trip if it exists. Members without a participant record come back with
     * {@code participantId = null} and {@code added = false}.
     */
    @Transactional(readOnly = true)
    public List<PassengerRowDTO> getPassengers(Long tripId) {
        ensureTripExists(tripId);

        Map<Long, TripParticipant> participantsByUserId = new HashMap<>();
        for (TripParticipant p : participantRepository.findByTripId(tripId)) {
            if (p.getPlayer() != null) {
                participantsByUserId.put(p.getPlayer().getId(), p);
            }
        }

        List<PassengerRowDTO> rows = new ArrayList<>();
        for (User user : teamMembers()) {
            TripParticipant participant = participantsByUserId.get(user.getId());
            rows.add(participant != null
                    ? PassengerRowDTO.fromParticipant(participant)
                    : PassengerRowDTO.fromUser(user));
        }
        return rows;
    }

    /**
     * Toggles a single team member on/off for the trip. Enabling creates (or re-enables)
     * the participant; disabling removes it. Other participants are left untouched.
     */
    @Transactional
    public PassengerRowDTO toggle(Long tripId, PassengerToggleRequest request) {
        Trip trip = findTripOrThrow(tripId);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + request.getUserId() + " was not found"));

        TripParticipant participant = participantRepository
                .findByTripIdAndPlayerId(tripId, user.getId())
                .orElse(null);

        if (request.isChecked()) {
            if (participant == null) {
                participant = new TripParticipant();
                participant.setTrip(trip);
                participant.setPlayer(user);
            }
            participant.setChecked(true);
            return PassengerRowDTO.fromParticipant(participantRepository.save(participant));
        }

        if (participant != null) {
            participantRepository.delete(participant);
        }
        return PassengerRowDTO.fromUser(user);
    }

    @Transactional
    public PassengerRowDTO updateDocumentation(Long tripId, Long participantId,
                                               DocumentationUpdateRequest request) {
        TripParticipant p = findParticipantInTrip(tripId, participantId);
        p.setDocumentationStatus(request.getStatus());
        return PassengerRowDTO.fromParticipant(participantRepository.save(p));
    }

    /**
     * Sets the documentation status for a team member who has no participant record yet.
     * Creates a participant with {@code checked = false} so the status is persisted
     * without adding the member to the trip.
     */
    @Transactional
    public PassengerRowDTO setDocumentationForNonParticipant(Long tripId,
                                                             PassengerDocumentationRequest request) {
        Trip trip = findTripOrThrow(tripId);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + request.getUserId() + " was not found"));

        TripParticipant participant = participantRepository
                .findByTripIdAndPlayerId(tripId, user.getId())
                .orElseGet(() -> {
                    TripParticipant created = new TripParticipant();
                    created.setTrip(trip);
                    created.setPlayer(user);
                    created.setChecked(false);
                    return created;
                });
        participant.setDocumentationStatus(request.getStatus());
        return PassengerRowDTO.fromParticipant(participantRepository.save(participant));
    }

    @Transactional
    public PassengerRowDTO assignRoom(Long tripId, Long participantId, RoomAssignRequest request) {
        TripParticipant p = findParticipantInTrip(tripId, participantId);
        p.setRoomNumber(request.getRoomNumber());
        return PassengerRowDTO.fromParticipant(participantRepository.save(p));
    }

    private List<User> teamMembers() {
        List<User> users = new ArrayList<>();
        for (Role role : TEAM_ROLES) {
            users.addAll(userRepository.findByRole(role));
        }
        users.sort((a, b) -> {
            int byFirst = safe(a.getFirstName()).compareTo(safe(b.getFirstName()));
            return byFirst != 0 ? byFirst : safe(a.getLastName()).compareTo(safe(b.getLastName()));
        });
        return users;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private TripParticipant findParticipantInTrip(Long tripId, Long participantId) {
        TripParticipant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participant with ID " + participantId + " was not found"));
        Long actualTripId = p.getTrip() != null ? p.getTrip().getId() : null;
        if (actualTripId == null || !actualTripId.equals(tripId)) {
            throw new ResourceNotFoundException(
                    "Participant " + participantId + " does not belong to trip " + tripId);
        }
        return p;
    }

    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip with ID " + tripId + " was not found"));
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
    }
}
