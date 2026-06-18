package com.iis.backend.service;

import com.iis.backend.dto.DocumentationUpdateRequest;
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
import java.util.Optional;

@Service
public class PassengerService {

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
            if (participant != null) {
                rows.add(PassengerRowDTO.fromParticipant(participant));
            } else {
                rows.add(PassengerRowDTO.fromUser(user));
            }
        }
        return rows;
    }

    @Transactional
    public PassengerRowDTO toggle(Long tripId, PassengerToggleRequest request) {
        if (request.isChecked()) {
            TripParticipant participant = findOrCreateParticipant(tripId, request.getUserId());
            participant.setChecked(true);
            return PassengerRowDTO.fromParticipant(participantRepository.save(participant));
        }

        TripParticipant participant = participantRepository
                .findByTripIdAndPlayerId(tripId, request.getUserId())
                .orElse(null);

        if (participant != null) {
            participantRepository.delete(participant);
        }

        return PassengerRowDTO.fromUser(findUserOrThrow(request.getUserId()));
    }

    @Transactional
    public PassengerRowDTO updateDocumentation(Long tripId, Long userId,
                                               DocumentationUpdateRequest request) {
        TripParticipant participant = findOrCreateParticipant(tripId, userId);
        participant.setDocumentationStatus(request.getStatus());
        return PassengerRowDTO.fromParticipant(participantRepository.save(participant));
    }

    @Transactional
    public PassengerRowDTO assignRoom(Long tripId, Long userId, RoomAssignRequest request) {
        TripParticipant participant = findOrCreateParticipant(tripId, userId);
        participant.setRoomNumber(request.getRoomNumber());
        return PassengerRowDTO.fromParticipant(participantRepository.save(participant));
    }

    private TripParticipant findOrCreateParticipant(Long tripId, Long userId) {
        Optional<TripParticipant> existing = participantRepository.findByTripIdAndPlayerId(tripId, userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Trip trip = findTripOrThrow(tripId);
        User user = findUserOrThrow(userId);
        TripParticipant participant = new TripParticipant();
        participant.setTrip(trip);
        participant.setPlayer(user);
        participant.setChecked(false);
        return participant;
    }

    private List<User> teamMembers() {
        List<User> users = new ArrayList<>();
        for (Role role : TEAM_ROLES) {
            users.addAll(userRepository.findByRole(role));
        }
        return users;
    }

    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip with ID " + tripId + " was not found"));
    }

    private User findUserOrThrow(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User with ID " + userId + " was not found");
        }
        return user.get();
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
    }
}
