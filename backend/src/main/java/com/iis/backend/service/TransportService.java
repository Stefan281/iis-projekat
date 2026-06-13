package com.iis.backend.service;

import com.iis.backend.dto.TransportCreateRequest;
import com.iis.backend.dto.TransportDTO;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Transport;
import com.iis.backend.model.Trip;
import com.iis.backend.repository.TransportRepository;
import com.iis.backend.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransportService {

    private final TransportRepository transportRepository;
    private final TripRepository tripRepository;

    public TransportService(TransportRepository transportRepository,
                            TripRepository tripRepository) {
        this.transportRepository = transportRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<TransportDTO> getOffers(Long tripId) {
        ensureTripExists(tripId);
        return transportRepository.findByTripId(tripId).stream()
                .map(TransportDTO::from)
                .toList();
    }

    @Transactional
    public TransportDTO create(Long tripId, TransportCreateRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with ID " + tripId + " was not found"));

        Transport t = new Transport();
        t.setTrip(trip);
        t.setCarrierName(request.getNaziv());
        t.setTransportType(request.getVrsta());
        t.setPrice(request.getCena());
        t.setSelected(false);

        return TransportDTO.from(transportRepository.save(t));
    }

    @Transactional
    public TransportDTO select(Long tripId, Long transportId) {
        ensureTripExists(tripId);
        List<Transport> all = transportRepository.findByTripId(tripId);
        Transport target = null;
        for (Transport t : all) {
            if (t.getId().equals(transportId)) {
                t.setSelected(true);
                target = t;
            } else if (t.isSelected()) {
                t.setSelected(false);
            }
        }
        if (target == null) {
            throw new ResourceNotFoundException(
                    "Transport " + transportId + " not found for trip " + tripId);
        }
        transportRepository.saveAll(all);
        return TransportDTO.from(target);
    }

    @Transactional(readOnly = true)
    public TransportDTO getSelected(Long tripId) {
        ensureTripExists(tripId);
        return transportRepository.findByTripIdAndSelectedTrue(tripId)
                .map(TransportDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No selected transport for trip " + tripId));
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with ID " + tripId + " was not found");
        }
    }
}
