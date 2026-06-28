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

@Entity
@Table(name = "recommendation_criteria")
public class RecommendationCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private RecommendationModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", nullable = false)
    private Metric metric;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CriterionComparison comparison;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "required_criterion", nullable = false)
    private boolean required;

    public RecommendationCriterion() {
    }

    public RecommendationCriterion(RecommendationModel model, Metric metric, CriterionComparison comparison, BigDecimal thresholdValue, BigDecimal weight, boolean required) {
        this.model = model;
        this.metric = metric;
        this.comparison = comparison;
        this.thresholdValue = thresholdValue;
        this.weight = weight;
        this.required = required;
    }

    public Long getId() { return id; }
    public RecommendationModel getModel() { return model; }
    public Metric getMetric() { return metric; }
    public CriterionComparison getComparison() { return comparison; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public BigDecimal getWeight() { return weight; }
    public boolean isRequired() { return required; }
}
