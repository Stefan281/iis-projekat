package com.iis.backend.service;

import com.iis.backend.dto.PriceBreakdown;
import com.iis.backend.dto.PriceTotalResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private static final BigDecimal EARLY_BIRD_DISCOUNT = BigDecimal.valueOf(0.90);
    private static final BigDecimal LAST_DAY_DISCOUNT = BigDecimal.valueOf(0.85);

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final PromotionService promotionService;

    public PricingService(
            SeatRepository seatRepository,
            TicketRepository ticketRepository,
            ReservationRepository reservationRepository,
            PromotionService promotionService) {
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.promotionService = promotionService;
    }

    public PriceBreakdown breakdown(Match match, Seat seat, Long ticketTypeId) {
        var zone = seat.getZone();
        var basePrice = match.getBasePrice();
        var zoneCoef = zone.getPriceCoefficient();
        var attractCoef = attractivenessCoefficient(match.getAttractiveness());
        var occupancyCoef = occupancyCoefficient(match.getId());

        var appliedRules = new ArrayList<PriceBreakdown.AppliedRule>();
        appliedRules.add(new PriceBreakdown.AppliedRule("Atraktivnost", attractCoef));
        appliedRules.add(new PriceBreakdown.AppliedRule("Popunjenost", occupancyCoef));

        var daysUntilMatch = ChronoUnit.DAYS.between(LocalDate.now(), match.getDate());
        BigDecimal dateCoef = BigDecimal.ONE;
        if (daysUntilMatch >= 30) {
            dateCoef = EARLY_BIRD_DISCOUNT;
            appliedRules.add(new PriceBreakdown.AppliedRule("Early bird", dateCoef));
        } else if (daysUntilMatch <= 0 && availableSeatsForMatch(match.getId()) > 0) {
            dateCoef = LAST_DAY_DISCOUNT;
            appliedRules.add(new PriceBreakdown.AppliedRule("Poslednji dan", dateCoef));
        }

        var priceBeforePromo = basePrice
                .multiply(zoneCoef)
                .multiply(attractCoef)
                .multiply(occupancyCoef)
                .multiply(dateCoef);

        var activePromo = promotionService.findBestActive();
        var promoDiscount = activePromo
                .map(p -> priceBeforePromo.multiply(p.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
        var promoName = activePromo.map(p -> p.getName()).orElse(null);
        var finalPrice = priceBeforePromo.subtract(promoDiscount).setScale(2, RoundingMode.HALF_UP);

        return new PriceBreakdown(
                basePrice, zoneCoef, zone.getName(),
                BigDecimal.ONE, "Standard",
                List.copyOf(appliedRules),
                promoDiscount, promoName,
                finalPrice);
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

    public PriceTotalResponse calculateTotal(Match match, List<Seat> seats, Long promotionId) {
        List<BigDecimal> prices = seats.stream()
                .map(seat -> calculatePrice(match, seat))
                .collect(java.util.stream.Collectors.toList());

        BigDecimal baseTotal = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO;
        String promotionName = null;

        if (promotionId != null) {
            try {
                var promo = promotionService.findById(promotionId);
                promotionName = promo.getName();
                discount = baseTotal
                            .multiply(promo.getDiscountPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } catch (Exception ignored) {}
        }

        BigDecimal finalTotal = baseTotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        return new PriceTotalResponse(baseTotal, discount.setScale(2, RoundingMode.HALF_UP), finalTotal, promotionName);
    }

    public BigDecimal applyPromotion(BigDecimal price, Long promotionId) {
        if (promotionId == null) return price;
        try {
            var promo = promotionService.findById(promotionId);
            var discount = price.multiply(promo.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return price.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return price;
        }
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
