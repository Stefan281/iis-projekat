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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PromotionService promotionService;

    public TicketService(
            TicketRepository ticketRepository,
            MatchService matchService,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            PricingService pricingService,
            PromotionService promotionService) {
        this.ticketRepository = ticketRepository;
        this.matchService = matchService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.pricingService = pricingService;
        this.promotionService = promotionService;
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

        // Pre-resolve seats and check availability before creating any ticket
        Map<Long, Seat> seatsById = new HashMap<>();
        Map<Long, Reservation> reservationsById = new HashMap<>();
        for (Long seatId : seatIds) {
            var seat = findSeat(seatId);
            var activeReservation = reservationRepository
                    .findByMatchIdAndSeatIdAndStatus(match.getId(), seat.getId(), ReservationStatus.ACTIVE)
                    .orElse(null);
            if (!isSeatAvailableForPurchase(match.getId(), customer, seat, activeReservation)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat is not available");
            }
            seatsById.put(seatId, seat);
            if (activeReservation != null) {
                reservationsById.put(seatId, activeReservation);
            }
        }

        // Calculate base prices for all seats
        Map<Long, BigDecimal> basePrices = new HashMap<>();
        for (Long seatId : seatIds) {
            basePrices.put(seatId, pricingService.calculatePrice(match, seatsById.get(seatId)));
        }

        for (Long seatId : seatIds) {
            BigDecimal price = pricingService.applyPromotion(basePrices.get(seatId), request.promotionId());

            var ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setMatch(match);
            ticket.setSeat(seatsById.get(seatId));
            ticket.setPrice(price);
            ticket.setStatus(TicketStatus.VALID);
            ticket.setPurchasedAt(LocalDateTime.now());
            tickets.add(toResponse(ticketRepository.save(ticket)));

            var activeReservation = reservationsById.get(seatId);
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
