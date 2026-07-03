package com.iis.backend.controller;

import com.iis.backend.dto.MatchSalesStats;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.ReservationStatus;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.TicketStatus;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.ReservationRepository;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.TicketRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final MatchRepository matchRepository;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public DashboardController(MatchRepository matchRepository, TicketRepository ticketRepository,
            ReservationRepository reservationRepository, SeatRepository seatRepository) {
        this.matchRepository = matchRepository;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/sales")
    public List<MatchSalesStats> salesStats() {
        var totalSeats = seatRepository.findAll().stream()
                .filter(s -> s.getStatus() != SeatStatus.BLOCKED)
                .count();

        return matchRepository.findAll().stream()
                .filter(m -> m.getStatus() != MatchStatus.CANCELLED)
                .map(match -> {
                    var sold = ticketRepository.countByMatchIdAndStatus(match.getId(), TicketStatus.VALID);
                    var reserved = reservationRepository.countByMatchIdAndStatus(match.getId(), ReservationStatus.ACTIVE);
                    var revenue = ticketRepository.sumRevenueByMatchIdAndStatus(match.getId(), TicketStatus.VALID)
                            .orElse(BigDecimal.ZERO);
                    var available = Math.max(0, totalSeats - sold - reserved);
                    return new MatchSalesStats(
                            match.getId(),
                            match.getHomeTeam(),
                            match.getAwayTeam(),
                            match.getDate().toString(),
                            sold,
                            reserved,
                            available,
                            revenue);
                })
                .toList();
    }
}
