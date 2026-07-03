package com.iis.backend.repository;

import com.iis.backend.model.Promotion;
import com.iis.backend.model.PromotionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findFirstByStatusOrderByDiscountPercentageDesc(PromotionStatus status);

    List<Promotion> findAllByStatus(PromotionStatus status);

    Optional<Promotion> findByPromoCodeIgnoreCase(String promoCode);
}
