package com.iis.backend.service;

import com.iis.backend.dto.PurchaseRequest;
import com.iis.backend.dto.TicketResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Seat;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.Ticket;
import com.iis.backend.model.TicketStatus;
import com.iis.backend.model.User;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public TicketService(TicketRepository ticketRepository, MatchService matchService, SeatRepository seatRepository) {
        this.ticketRepository = ticketRepository;
        this.matchService = matchService;
        this.seatRepository = seatRepository;
    }

    public List<TicketResponse> findByCustomer(User customer) {
        return ticketRepository.findByCustomerIdOrderByPurchasedAtDesc(customer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse purchase(User customer, PurchaseRequest request) {
        var match = matchService.findById(request.matchId());
        var seat = findSeat(request.seatId());

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat is not available");
        }

        seat.setStatus(SeatStatus.SOLD);
        seatRepository.save(seat);

        var ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setMatch(match);
        ticket.setSeat(seat);
        ticket.setPrice(calculatePrice(match.getBasePrice(), seat));
        ticket.setStatus(TicketStatus.VALID);
        ticket.setPurchasedAt(LocalDateTime.now());

        return toResponse(ticketRepository.save(ticket));
    }

    private Seat findSeat(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat was not found"));
    }

    private BigDecimal calculatePrice(BigDecimal basePrice, Seat seat) {
        return basePrice.multiply(seat.getZone().getPriceCoefficient());
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
