package com.iis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private PricingRule pricingRule;

    @Column(precision = 6, scale = 4)
    private BigDecimal oldCoefficient;

    @Column(precision = 6, scale = 4)
    private BigDecimal newCoefficient;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceHistorySource source;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public PricingRule getPricingRule() { return pricingRule; }
    public void setPricingRule(PricingRule pricingRule) { this.pricingRule = pricingRule; }
    public BigDecimal getOldCoefficient() { return oldCoefficient; }
    public void setOldCoefficient(BigDecimal oldCoefficient) { this.oldCoefficient = oldCoefficient; }
    public BigDecimal getNewCoefficient() { return newCoefficient; }
    public void setNewCoefficient(BigDecimal newCoefficient) { this.newCoefficient = newCoefficient; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PriceHistorySource getSource() { return source; }
    public void setSource(PriceHistorySource source) { this.source = source; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
