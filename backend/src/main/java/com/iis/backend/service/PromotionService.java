package com.iis.backend.service;

import com.iis.backend.dto.PromotionRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Promotion;
import com.iis.backend.repository.PromotionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> findAll() { return promotionRepository.findAll(); }

    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion was not found"));
    }

    public Promotion create(PromotionRequest request) {
        return promotionRepository.save(mapToEntity(new Promotion(), request));
    }

    public Promotion update(Long id, PromotionRequest request) {
        return promotionRepository.save(mapToEntity(findById(id), request));
    }

    public void delete(Long id) {
        promotionRepository.delete(findById(id));
    }

    private Promotion mapToEntity(Promotion promotion, PromotionRequest request) {
        promotion.setName(request.name());
        promotion.setDiscountPercentage(request.discountPercentage());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setStatus(request.status());
        return promotion;
    }
}
