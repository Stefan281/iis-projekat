package com.iis.backend.service;

import com.iis.backend.dto.PricingRuleRequest;
import com.iis.backend.dto.PricingRuleResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.PriceHistorySource;
import com.iis.backend.model.PricingRule;
import com.iis.backend.repository.PricingRuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PricingRuleService {
    private final PricingRuleRepository repository;
    private final PriceHistoryService priceHistoryService;

    public PricingRuleService(PricingRuleRepository repository, PriceHistoryService priceHistoryService) {
        this.repository = repository;
        this.priceHistoryService = priceHistoryService;
    }

    public List<PricingRuleResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public PricingRuleResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public PricingRuleResponse create(PricingRuleRequest request) {
        var rule = new PricingRule();
        applyRequest(rule, request);
        return toResponse(repository.save(rule));
    }

    public PricingRuleResponse update(Long id, PricingRuleRequest request) {
        var rule = getOrThrow(id);
        var oldCoefficient = rule.getCoefficient();
        applyRequest(rule, request);
        var saved = repository.save(rule);
        if (oldCoefficient.compareTo(request.coefficient()) != 0) {
            priceHistoryService.recordRuleChange(saved, oldCoefficient, request.coefficient(), PriceHistorySource.MANAGER);
        }
        return toResponse(saved);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    PricingRule getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found"));
    }

    private void applyRequest(PricingRule rule, PricingRuleRequest request) {
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setCondition(request.condition());
        rule.setCoefficient(request.coefficient());
        rule.setActive(request.active());
        rule.setPriority(request.priority());
    }

    private PricingRuleResponse toResponse(PricingRule rule) {
        return new PricingRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getCondition(),
                rule.getCoefficient(),
                rule.isActive(),
                rule.getPriority());
    }
}
