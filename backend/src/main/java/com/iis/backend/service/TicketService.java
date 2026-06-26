package com.iis.backend.service;

import com.iis.backend.dto.PurchaseRequest;
import com.iis.backend.dto.TicketResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Reservation;
import com.iis.backend.model.ReservationStatus;
import com.iis.backend.model.Seat;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.Ticket;
import com.iis.backend.model.TicketStatus;
import com.iis.backend.model.User;
import com.iis.backend.repository.ReservationRepository;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final MatchService matchService;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;

    public TicketService(
            TicketRepository ticketRepository,
            MatchService matchService,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            PricingService pricingService) {
        this.ticketRepository = ticketRepository;
        this.matchService = matchService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.pricingService = pricingService;
    }

    public List<TicketResponse> findByCustomer(User customer) {
        return ticketRepository.findByCustomerIdOrderByPurchasedAtDesc(customer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<TicketResponse> purchase(User customer, PurchaseRequest request) {
        var match = matchService.findById(request.matchId());
        var seatIds = requestedSeatIds(request);
        var tickets = new ArrayList<TicketResponse>();

        for (Long seatId : seatIds) {
            var seat = findSeat(seatId);
            var activeReservation = reservationRepository
                    .findByMatchIdAndSeatIdAndStatus(match.getId(), seat.getId(), ReservationStatus.ACTIVE)
                    .orElse(null);

            if (!isSeatAvailableForPurchase(match.getId(), customer, seat, activeReservation)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat is not available");
            }

            var ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setMatch(match);
            ticket.setSeat(seat);
            ticket.setPrice(pricingService.calculatePrice(match, seat));
            ticket.setStatus(TicketStatus.VALID);
            ticket.setPurchasedAt(LocalDateTime.now());
            tickets.add(toResponse(ticketRepository.save(ticket)));

            if (activeReservation != null) {
                activeReservation.setStatus(ReservationStatus.SOLD);
                reservationRepository.save(activeReservation);
            }
        }

        return tickets;
    }

    private Seat findSeat(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat was not found"));
    }

    private boolean isSeatAvailableForPurchase(Long matchId, User customer, Seat seat, Reservation activeReservation) {
        if (seat.getStatus() == SeatStatus.BLOCKED) {
            return false;
        }

        var sold = ticketRepository.existsByMatchIdAndSeatIdAndStatus(matchId, seat.getId(), TicketStatus.VALID);
        if (sold) {
            return false;
        }

        if (activeReservation != null) {
            return activeReservation.getCustomer().getId().equals(customer.getId());
        }

        return true;
    }

    private List<Long> requestedSeatIds(PurchaseRequest request) {
        if (request.seatIds() != null && !request.seatIds().isEmpty()) {
            return request.seatIds();
        }
        if (request.seatId() != null) {
            return List.of(request.seatId());
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one seat must be selected");
    }

    private TicketResponse toResponse(Ticket ticket) {
        var match = ticket.getMatch();
        var seat = ticket.getSeat();
        var zone = seat.getZone();

        return new TicketResponse(
                ticket.getId(),
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
                ticket.getPrice(),
                ticket.getStatus(),
                ticket.getPurchasedAt());
    }
}
