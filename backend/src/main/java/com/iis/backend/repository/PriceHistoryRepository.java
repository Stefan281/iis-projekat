package com.iis.backend.repository;

import com.iis.backend.model.PriceHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByPricingRuleIdOrderByChangedAtDesc(Long pricingRuleId);
    List<PriceHistory> findAllByOrderByChangedAtDesc();
}
