package com.iis.backend.repository;

import com.iis.backend.model.PricingRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByActiveTrueOrderByPriorityAsc();
}
