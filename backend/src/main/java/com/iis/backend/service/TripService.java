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
import java.util.ArrayList;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional
    public List<TripResponse> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        List<TripResponse> responses = new ArrayList<>();
        for (Trip trip : trips) {
            syncStatusIfNeeded(trip);
            responses.add(TripResponse.from(trip));
        }
        return responses;
    }

    private void syncStatusIfNeeded(Trip trip) {
        if (trip.getStatus() == TripStatus.COMPLETED) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (trip.getReturnDate() != null) {
            if (trip.getReturnDate().isBefore(today)) {
                trip.setStatus(TripStatus.COMPLETED);
                tripRepository.save(trip);
            }
        } else {
            if (trip.getDepartureDate() != null && trip.getDepartureDate().isBefore(today)) {
                trip.setStatus(TripStatus.COMPLETED);
                tripRepository.save(trip);
            }
        }
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trip trip = findTripOrThrow(id);
        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        if (request.getDepartureDate() != null && request.getDepartureDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Nije moguće kreirati putovanje sa datumima koji su već prošli.");
        }
        checkDateOverlap(request.getDepartureDate(), request.getReturnDate(), null);
        Trip trip = new Trip();
        applyRequest(trip, request);
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request) {
        Trip trip = findTripOrThrow(id);
        if (trip.getDepartureDate() != null && trip.getDepartureDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Nije moguće menjati putovanje koje je već počelo.");
        }
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

            // ili je jedno pre pocetka drugog skroz ili je skroz posle kraja, inace se poklapa
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
