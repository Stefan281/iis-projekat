package com.iis.backend.repository;

import com.iis.backend.model.TripParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripParticipantRepository extends JpaRepository<TripParticipant, Long> {
    List<TripParticipant> findByTripId(Long tripId);
    List<TripParticipant> findByPlayerId(Long playerId);
}
