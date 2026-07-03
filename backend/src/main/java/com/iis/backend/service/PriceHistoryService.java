package com.iis.backend.service;

import com.iis.backend.dto.PriceHistoryResponse;
import com.iis.backend.model.PriceHistory;
import com.iis.backend.model.PriceHistorySource;
import com.iis.backend.model.PricingRule;
import com.iis.backend.repository.PriceHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriceHistoryService {
    private final PriceHistoryRepository repository;

    public PriceHistoryService(PriceHistoryRepository repository) {
        this.repository = repository;
    }

    public List<PriceHistoryResponse> findAll() {
        return repository.findAllByOrderByChangedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<PriceHistoryResponse> findByRule(Long ruleId) {
        return repository.findByPricingRuleIdOrderByChangedAtDesc(ruleId).stream().map(this::toResponse).toList();
    }

    public void recordRuleChange(PricingRule rule, BigDecimal oldCoefficient, BigDecimal newCoefficient, PriceHistorySource source) {
        var entry = new PriceHistory();
        entry.setPricingRule(rule);
        entry.setOldCoefficient(oldCoefficient);
        entry.setNewCoefficient(newCoefficient);
        entry.setDescription("Coefficient changed for rule: " + rule.getName());
        entry.setSource(source);
        entry.setChangedAt(LocalDateTime.now());
        repository.save(entry);
    }

    public void recordSystemPriceApplication(PricingRule rule, BigDecimal appliedCoefficient, String context) {
        var entry = new PriceHistory();
        entry.setPricingRule(rule);
        entry.setNewCoefficient(appliedCoefficient);
        entry.setDescription("Rule applied: " + rule.getName() + " — " + context);
        entry.setSource(PriceHistorySource.SYSTEM);
        entry.setChangedAt(LocalDateTime.now());
        repository.save(entry);
    }

    private PriceHistoryResponse toResponse(PriceHistory h) {
        var rule = h.getPricingRule();
        return new PriceHistoryResponse(
                h.getId(),
                rule != null ? rule.getId() : null,
                rule != null ? rule.getName() : null,
                h.getOldCoefficient(),
                h.getNewCoefficient(),
                h.getDescription(),
                h.getSource(),
                h.getChangedAt());
    }
}
