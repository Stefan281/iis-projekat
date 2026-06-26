package com.iis.backend.service;

import com.iis.backend.dto.SeatRequest;
import com.iis.backend.dto.SeatResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.ReservationStatus;
import com.iis.backend.model.Seat;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.TicketStatus;
import com.iis.backend.repository.ReservationRepository;
import com.iis.backend.repository.TicketRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final ZoneService zoneService;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;

    public SeatService(
            SeatRepository seatRepository,
            ZoneService zoneService,
            TicketRepository ticketRepository,
            ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.zoneService = zoneService;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<SeatResponse> findAll() {
        return seatRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SeatResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    public List<SeatResponse> findByZone(Long zoneId) {
        return seatRepository.findByZoneId(zoneId).stream().map(this::toResponse).toList();
    }

    public List<SeatResponse> findAllForMatch(Long matchId) {
        return seatRepository.findAll().stream()
                .map(seat -> toResponseForMatch(seat, matchId))
                .toList();
    }

    public List<SeatResponse> findByZoneForMatch(Long zoneId, Long matchId) {
        return seatRepository.findByZoneId(zoneId).stream()
                .map(seat -> toResponseForMatch(seat, matchId))
                .toList();
    }

    public SeatResponse create(SeatRequest request) {
        validateUniqueSeat(null, request);
        return toResponse(seatRepository.save(mapToEntity(new Seat(), request)));
    }

    public SeatResponse update(Long id, SeatRequest request) {
        validateUniqueSeat(id, request);
        return toResponse(seatRepository.save(mapToEntity(findEntityById(id), request)));
    }

    public void delete(Long id) {
        seatRepository.delete(findEntityById(id));
    }

    private Seat findEntityById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat was not found"));
    }

    private Seat mapToEntity(Seat seat, SeatRequest request) {
        seat.setRowLabel(request.rowLabel().trim());
        seat.setSeatNumber(request.seatNumber());
        seat.setStatus(request.status());
        seat.setZone(zoneService.findById(request.zoneId()));
        return seat;
    }

    private void validateUniqueSeat(Long currentSeatId, SeatRequest request) {
        var rowLabel = request.rowLabel().trim();
        var exists = currentSeatId == null
                ? seatRepository.existsByZoneIdAndRowLabelIgnoreCaseAndSeatNumber(
                        request.zoneId(), rowLabel, request.seatNumber())
                : seatRepository.existsByZoneIdAndRowLabelIgnoreCaseAndSeatNumberAndIdNot(
                        request.zoneId(), rowLabel, request.seatNumber(), currentSeatId);

        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sediste vec postoji.");
        }
    }

    private SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getZone().getId(),
                seat.getZone().getName());
    }

    private SeatResponse toResponseForMatch(Seat seat, Long matchId) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                statusForMatch(seat, matchId),
                seat.getZone().getId(),
                seat.getZone().getName());
    }

    private SeatStatus statusForMatch(Seat seat, Long matchId) {
        if (seat.getStatus() == SeatStatus.BLOCKED) {
            return SeatStatus.BLOCKED;
        }

        if (ticketRepository.existsByMatchIdAndSeatIdAndStatus(matchId, seat.getId(), TicketStatus.VALID)) {
            return SeatStatus.SOLD;
        }

        if (reservationRepository.existsByMatchIdAndSeatIdAndStatus(matchId, seat.getId(), ReservationStatus.ACTIVE)) {
            return SeatStatus.RESERVED;
        }

        return SeatStatus.AVAILABLE;
    }
}
