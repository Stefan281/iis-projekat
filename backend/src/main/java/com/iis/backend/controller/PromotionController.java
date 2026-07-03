package com.iis.backend.controller;

import com.iis.backend.dto.PromotionRequest;
import com.iis.backend.model.Promotion;
import com.iis.backend.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<Promotion> findAll() { return promotionService.findAll(); }

    @GetMapping("/active")
    public List<Promotion> findActive() { return promotionService.findAllActive(); }

    @GetMapping("/{id}")
    public Promotion findById(@PathVariable Long id) { return promotionService.findById(id); }

    @GetMapping("/by-code/{code}")
    public Promotion findByCode(@PathVariable String code) {
        return promotionService.findByPromoCode(code)
                .orElseThrow(() -> new com.iis.backend.exception.ResourceNotFoundException("Promo kod nije pronađen"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion create(@Valid @RequestBody PromotionRequest request) { return promotionService.create(request); }

    @PutMapping("/{id}")
    public Promotion update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        return promotionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { promotionService.delete(id); }
}
