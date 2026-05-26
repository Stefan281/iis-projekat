package com.iis.backend.service;

import com.iis.backend.dto.TripRequest;
import com.iis.backend.dto.TripResponse;
import com.iis.backend.dto.TripStatusUpdateRequest;
import com.iis.backend.enums.TripStatus;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Trip;
import com.iis.backend.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Trip trip = new Trip();
        applyRequest(trip, request);
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request) {
        Trip trip = findTripOrThrow(id);
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
            trip.setRejectionReason(request.getRejectionReason());
        } else {
            trip.setRejectionReason(null);
        }
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
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
