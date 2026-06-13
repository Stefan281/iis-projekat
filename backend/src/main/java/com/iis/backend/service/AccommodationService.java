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

import java.util.List;

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
        return accommodationRepository.findByTripId(tripId).stream()
                .map(AccommodationDTO::from)
                .toList();
    }

    @Transactional
    public AccommodationDTO create(Long tripId, AccommodationCreateRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with ID " + tripId + " was not found"));

        Accommodation a = new Accommodation();
        a.setTrip(trip);
        a.setName(request.getIme());
        a.setAddress(request.getAdresa());
        a.setPrice(request.getCena());
        a.setSelected(false);

        return AccommodationDTO.from(accommodationRepository.save(a));
    }

    @Transactional
    public AccommodationDTO select(Long tripId, Long accommodationId) {
        ensureTripExists(tripId);
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
        return accommodationRepository.findByTripIdAndSelectedTrue(tripId)
                .map(AccommodationDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No selected accommodation for trip " + tripId));
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
    }
}
