package com.iis.backend.service;

import com.iis.backend.dto.TripRequest;
import com.iis.backend.dto.TripResponse;
import com.iis.backend.dto.TripStatusUpdateRequest;
import com.iis.backend.enums.TripStatus;
import com.iis.backend.exception.DateOverlapException;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Trip;
import com.iis.backend.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(TripResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trip trip = findTripOrThrow(id);
        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        checkDateOverlap(request.getDepartureDate(), request.getReturnDate(), null);
        Trip trip = new Trip();
        applyRequest(trip, request);
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request) {
        Trip trip = findTripOrThrow(id);
        checkDateOverlap(request.getDepartureDate(), request.getReturnDate(), id);
        applyRequest(trip, request);
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = findTripOrThrow(id);
        tripRepository.delete(trip);
    }

    @Transactional
    public TripResponse updateStatus(Long id, TripStatusUpdateRequest request) {
        Trip trip = findTripOrThrow(id);
        trip.setStatus(request.getStatus());
        if (request.getStatus() == TripStatus.REJECTED) {
            trip.setRazlogOdbijanja(request.getRazlogOdbijanja());
        } else {
            trip.setRazlogOdbijanja(null);
        }
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    /**
     * Rejects a trip whose dates overlap with any other trip. A missing return date is
     * treated as a single-day trip ({@code end = start}). When {@code excludeId} is set
     * (update) that trip is excluded so it does not clash with itself.
     */
    private void checkDateOverlap(LocalDate start, LocalDate end, Long excludeId) {
        if (start == null) {
            return;
        }
        LocalDate newStart = start;
        LocalDate newEnd = end != null ? end : start;

        List<Trip> others = excludeId != null
                ? tripRepository.findByIdNot(excludeId)
                : tripRepository.findAll();

        for (Trip other : others) {
            LocalDate otherStart = other.getDepartureDate();
            if (otherStart == null) {
                continue;
            }
            LocalDate otherEnd = other.getReturnDate() != null ? other.getReturnDate() : otherStart;

            // Two date ranges overlap unless one ends strictly before the other begins.
            if (!newStart.isAfter(otherEnd) && !newEnd.isBefore(otherStart)) {
                throw new DateOverlapException(
                        "Putovanje se poklapa sa postojećim putovanjem: " + other.getName());
            }
        }
    }

    private Trip findTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip with ID " + id + " was not found"));
    }

    private void applyRequest(Trip trip, TripRequest request) {
        trip.setName(request.getName());
        trip.setLocation(request.getLocation());
        trip.setPurpose(request.getPurpose());
        trip.setDepartureDate(request.getDepartureDate());
        trip.setReturnDate(request.getReturnDate());
        trip.setStatus(request.getStatus());
    }
}
