package com.iis.backend.service;

import com.iis.backend.dto.AccommodationCreateRequest;
import com.iis.backend.dto.AccommodationDTO;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Accommodation;
import com.iis.backend.model.Trip;
import com.iis.backend.repository.AccommodationRepository;
import com.iis.backend.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final TripRepository tripRepository;

    public AccommodationService(AccommodationRepository accommodationRepository,
                                TripRepository tripRepository) {
        this.accommodationRepository = accommodationRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<AccommodationDTO> getOffers(Long tripId) {
        ensureTripExists(tripId);
        List<AccommodationDTO> offers = new ArrayList<>();
        for (Accommodation a : accommodationRepository.findByTripId(tripId)) {
            offers.add(AccommodationDTO.from(a));
        }
        return offers;
    }

    @Transactional
    public AccommodationDTO create(Long tripId, AccommodationCreateRequest request) {
        Trip trip = findTripOrThrow(tripId);
        checkTripNotStarted(trip);

        Accommodation a = new Accommodation();
        a.setTrip(trip);
        a.setName(request.getName());
        a.setAddress(request.getAddress());
        a.setPrice(request.getPrice());
        a.setSelected(false);

        return AccommodationDTO.from(accommodationRepository.save(a));
    }

    @Transactional
    public AccommodationDTO select(Long tripId, Long accommodationId) {
        Trip trip = findTripOrThrow(tripId);
        checkTripNotStarted(trip);
        List<Accommodation> all = accommodationRepository.findByTripId(tripId);
        Accommodation target = null;
        for (Accommodation a : all) {
            if (a.getId().equals(accommodationId)) {
                a.setSelected(true);
                target = a;
            } else if (a.isSelected()) {
                a.setSelected(false);
            }
        }
        if (target == null) {
            throw new ResourceNotFoundException(
                    "Accommodation " + accommodationId + " not found for trip " + tripId);
        }
        accommodationRepository.saveAll(all);
        return AccommodationDTO.from(target);
    }

    @Transactional(readOnly = true)
    public AccommodationDTO getSelected(Long tripId) {
        ensureTripExists(tripId);
        Optional<Accommodation> selected = accommodationRepository.findByTripIdAndSelectedTrue(tripId);
        if (selected.isEmpty()) {
            throw new ResourceNotFoundException("No selected accommodation for trip " + tripId);
        }
        return AccommodationDTO.from(selected.get());
    }

    private Trip findTripOrThrow(Long tripId) {
        Optional<Trip> trip = tripRepository.findById(tripId);
        if (trip.isEmpty()) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
        return trip.get();
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
    }

    private void checkTripNotStarted(Trip trip) {
        if (trip.getDepartureDate() != null && trip.getDepartureDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Nije moguće menjati putovanje koje je već počelo.");
        }
    }
}
