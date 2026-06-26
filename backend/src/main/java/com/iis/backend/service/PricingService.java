package com.iis.backend.service;

import com.iis.backend.model.Match;
import com.iis.backend.model.MatchAttractiveness;
import com.iis.backend.model.Seat;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.repository.ReservationRepository;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.TicketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private static final BigDecimal EARLY_BIRD_DISCOUNT = BigDecimal.valueOf(0.90);
    private static final BigDecimal LAST_DAY_DISCOUNT = BigDecimal.valueOf(0.85);

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;

    public PricingService(
            SeatRepository seatRepository,
            TicketRepository ticketRepository,
            ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
    }

    public BigDecimal calculatePrice(Match match, Seat seat) {
        var price = match.getBasePrice()
                .multiply(seat.getZone().getPriceCoefficient())
                .multiply(attractivenessCoefficient(match.getAttractiveness()))
                .multiply(occupancyCoefficient(match.getId()));

        var daysUntilMatch = ChronoUnit.DAYS.between(LocalDate.now(), match.getDate());
        if (daysUntilMatch >= 30) {
            price = price.multiply(EARLY_BIRD_DISCOUNT);
        } else if (daysUntilMatch <= 0 && availableSeatsForMatch(match.getId()) > 0) {
            price = price.multiply(LAST_DAY_DISCOUNT);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal attractivenessCoefficient(MatchAttractiveness attractiveness) {
        return switch (attractiveness) {
            case LOW -> BigDecimal.valueOf(0.95);
            case MEDIUM -> BigDecimal.ONE;
            case HIGH -> BigDecimal.valueOf(1.15);
            case DERBY -> BigDecimal.valueOf(1.30);
        };
    }

    private BigDecimal occupancyCoefficient(Long matchId) {
        var totalSeats = seatRepository.count();
        if (totalSeats == 0) {
            return BigDecimal.ONE;
        }

        var sold = ticketRepository.countByMatchIdAndStatus(matchId, com.iis.backend.model.TicketStatus.VALID);
        var reserved = reservationRepository.countByMatchIdAndStatus(matchId, com.iis.backend.model.ReservationStatus.ACTIVE);
        var occupancy = BigDecimal.valueOf(sold + reserved)
                .divide(BigDecimal.valueOf(totalSeats), 4, RoundingMode.HALF_UP);

        if (occupancy.compareTo(BigDecimal.valueOf(0.80)) >= 0) {
            return BigDecimal.valueOf(1.20);
        }
        if (occupancy.compareTo(BigDecimal.valueOf(0.50)) >= 0) {
            return BigDecimal.valueOf(1.10);
        }
        return BigDecimal.ONE;
    }

    private long availableSeatsForMatch(Long matchId) {
        var totalSeats = seatRepository.findAll().stream()
                .filter(seat -> seat.getStatus() != SeatStatus.BLOCKED)
                .count();
        var sold = ticketRepository.countByMatchIdAndStatus(matchId, com.iis.backend.model.TicketStatus.VALID);
        var reserved = reservationRepository.countByMatchIdAndStatus(matchId, com.iis.backend.model.ReservationStatus.ACTIVE);
        return totalSeats - sold - reserved;
    }
}
