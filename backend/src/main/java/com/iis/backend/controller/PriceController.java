package com.iis.backend.controller;

import com.iis.backend.dto.PriceBreakdown;
import com.iis.backend.dto.PriceHistoryResponse;
import com.iis.backend.dto.PriceTotalResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.service.PriceHistoryService;
import com.iis.backend.service.PricingService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PriceController {
    private final PricingService pricingService;
    private final PriceHistoryService priceHistoryService;
    private final MatchRepository matchRepository;
    private final SeatRepository seatRepository;

    public PriceController(PricingService pricingService, PriceHistoryService priceHistoryService,
            MatchRepository matchRepository, SeatRepository seatRepository) {
        this.pricingService = pricingService;
        this.priceHistoryService = priceHistoryService;
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/breakdown")
    public PriceBreakdown breakdown(
            @RequestParam Long matchId,
            @RequestParam Long seatId,
            @RequestParam Long ticketTypeId) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        var seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        return pricingService.breakdown(match, seat, ticketTypeId);
    }

    @GetMapping("/total")
    public PriceTotalResponse total(
            @RequestParam Long matchId,
            @RequestParam List<Long> seatIds,
            @RequestParam(required = false) Long promotionId) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        var seats = seatIds.stream()
                .map(id -> seatRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found")))
                .collect(Collectors.toList());
        return pricingService.calculateTotal(match, seats, promotionId);
    }

    @GetMapping("/history")
    public List<PriceHistoryResponse> history(@RequestParam(required = false) Long ruleId) {
        if (ruleId != null) {
            return priceHistoryService.findByRule(ruleId);
        }
        return priceHistoryService.findAll();
    }
}
