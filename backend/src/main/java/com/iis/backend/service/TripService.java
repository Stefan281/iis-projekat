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
import java.util.Date;
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
        checkOverlapCreate(request.getDepartureDate(), request.getReturnDate());
        Trip trip = new Trip();
        applyRequest(trip, request);
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request) {
        Trip trip = findTripOrThrow(id);
        checkOverlapUpdate(request.getDepartureDate(), request.getReturnDate(), id);
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
        LocalDate danas= LocalDate.now();
        if(trip.getReturnDate().isBefore(danas)){
            throw new DateOverlapException("nije moguce izmeniti putovanje koje je vec proslo");
        }
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

    private void checkOverlapCreate(LocalDate start, LocalDate end){
        if(start==null){
            return;
        }
        if(end==null){
            end=start;
        }

        List<Trip> trips=tripRepository.findAll();

        for(Trip t:trips){
            LocalDate start2=t.getDepartureDate();
            LocalDate end2=t.getReturnDate();
            if(start2==null) { continue;}

            //za jednodnevna
            if(end2==null){end2=start2;}

            // preklapa se ako nije skroz posle i nije skroz pre
            if(!start.isAfter(end2) && !end.isBefore(start2)){
                throw new DateOverlapException("Datumi se preklapaju sa postojecim putovanjem: " + t.getName());
            }
        }
    }

    private void checkOverlapUpdate(LocalDate start, LocalDate end, Long id){
        if(start==null){
            return;
        }
        if(end==null){
            end=start;
        }

        List<Trip> trips = tripRepository.findByIdNot(id);

        for(Trip t:trips){
            LocalDate start2=t.getDepartureDate();
            LocalDate end2=t.getReturnDate();
            if(start2==null) { continue;}

            //za jednodnevna
            if(end2==null){end2=start2;}

            // preklapa se ako nije skroz posle i nije skroz pre
            if(!start.isAfter(end2) && !end.isBefore(start2)){
                throw new DateOverlapException("Datumi se preklapaju sa postojecim putovanjem: " + t.getName());
            }
        }
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
