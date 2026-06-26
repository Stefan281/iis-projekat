package com.iis.backend.service;

import com.iis.backend.dto.ReservationRequest;
import com.iis.backend.dto.ReservationResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Reservation;
import com.iis.backend.model.ReservationStatus;
import com.iis.backend.model.Role;
import com.iis.backend.model.Seat;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.Ticket;
import com.iis.backend.model.TicketStatus;
import com.iis.backend.model.User;
import com.iis.backend.repository.ReservationRepository;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MatchService matchService;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final PricingService pricingService;

    public ReservationService(
            ReservationRepository reservationRepository,
            MatchService matchService,
            SeatRepository seatRepository,
            TicketRepository ticketRepository,
            PricingService pricingService) {
        this.reservationRepository = reservationRepository;
        this.matchService = matchService;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.pricingService = pricingService;
    }

    public List<ReservationResponse> findByCustomer(User customer) {
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse reserve(User customer, ReservationRequest request) {
        var match = matchService.findById(request.matchId());
        var seat = findSeat(request.seatId());

        if (!isSeatAvailableForMatch(match.getId(), seat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat is not available");
        }

        var now = LocalDateTime.now();
        var reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setMatch(match);
        reservation.setSeat(seat);
        reservation.setPrice(pricingService.calculatePrice(match, seat));
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCreatedAt(now);
        reservation.setExpiresAt(now.toLocalDate().plusDays(1).atTime(17, 0));

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancel(User customer, Long reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation was not found"));

        if (customer.getRole() == Role.CUSTOMER && !reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to current user");
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse confirm(User manager, Long reservationId) {
        if (manager.getRole() == Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot confirm reservations");
        }

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation was not found"));

        if (reservation.getStatus() == ReservationStatus.SOLD) {
            return toResponse(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active reservations can be confirmed");
        }

        var match = reservation.getMatch();
        var seat = reservation.getSeat();
        if (ticketRepository.existsByMatchIdAndSeatIdAndStatus(match.getId(), seat.getId(), TicketStatus.VALID)) {
            reservation.setStatus(ReservationStatus.SOLD);
            return toResponse(reservationRepository.save(reservation));
        }

        var ticket = new Ticket();
        ticket.setCustomer(reservation.getCustomer());
        ticket.setMatch(match);
        ticket.setSeat(seat);
        ticket.setPrice(reservation.getPrice());
        ticket.setStatus(TicketStatus.VALID);
        ticket.setPurchasedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        reservation.setStatus(ReservationStatus.SOLD);
        return toResponse(reservationRepository.save(reservation));
    }

    private Seat findSeat(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat was not found"));
    }

    private boolean isSeatAvailableForMatch(Long matchId, Seat seat) {
        if (seat.getStatus() == SeatStatus.BLOCKED) {
            return false;
        }

        var sold = ticketRepository.existsByMatchIdAndSeatIdAndStatus(matchId, seat.getId(), TicketStatus.VALID);
        var reserved = reservationRepository.existsByMatchIdAndSeatIdAndStatus(matchId, seat.getId(), ReservationStatus.ACTIVE);
        return !sold && !reserved;
    }

    private ReservationResponse toResponse(Reservation reservation) {
        var match = reservation.getMatch();
        var seat = reservation.getSeat();
        var zone = seat.getZone();
        var customer = reservation.getCustomer();

        return new ReservationResponse(
                reservation.getId(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                match.getId(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getDate(),
                match.getTime(),
                match.getLocation(),
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                zone.getId(),
                zone.getName(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getExpiresAt());
    }
}
