package com.iis.backend.service;

import com.iis.backend.dto.PromotionRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Promotion;
import com.iis.backend.model.PromotionStatus;
import com.iis.backend.repository.PromotionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public Optional<Promotion> findBestActive() {
        return promotionRepository.findFirstByStatusOrderByDiscountPercentageDesc(PromotionStatus.ACTIVE);
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

    public List<Promotion> findAllActive() {
        return promotionRepository.findAllByStatus(PromotionStatus.ACTIVE);
    }

    public Optional<Promotion> findByPromoCode(String code) {
        return promotionRepository.findByPromoCodeIgnoreCase(code.trim());
    }

    private Promotion mapToEntity(Promotion promotion, PromotionRequest request) {
        promotion.setName(request.name());
        promotion.setDiscountPercentage(request.discountPercentage());
        promotion.setStatus(request.active() ? PromotionStatus.ACTIVE : PromotionStatus.INACTIVE);
        promotion.setMinTickets(Math.max(1, request.minTickets()));
        if (request.promotionType() != null) {
            promotion.setPromotionType(request.promotionType());
        }
        promotion.setPromoCode(request.promoCode() != null && !request.promoCode().isBlank()
                ? request.promoCode().trim().toUpperCase() : null);
        promotion.setStartDate(LocalDate.now());
        promotion.setEndDate(LocalDate.of(2099, 12, 31));
        return promotion;
    }
}
