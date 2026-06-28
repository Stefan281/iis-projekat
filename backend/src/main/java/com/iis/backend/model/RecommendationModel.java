package com.iis.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation_models")
public class RecommendationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String position;

    @Column(name = "minimum_height")
    private Integer minimumHeight;

    @Column(name = "minimum_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal minimumScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendationCriterion> criteria = new ArrayList<>();

    public RecommendationModel() {
    }

    public RecommendationModel(String name, String position, Integer minimumHeight, BigDecimal minimumScore, User createdBy, LocalDateTime createdAt) {
        this.name = name;
        this.position = position;
        this.minimumHeight = minimumHeight;
        this.minimumScore = minimumScore;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Integer getMinimumHeight() { return minimumHeight; }
    public void setMinimumHeight(Integer minimumHeight) { this.minimumHeight = minimumHeight; }
    public BigDecimal getMinimumScore() { return minimumScore; }
    public void setMinimumScore(BigDecimal minimumScore) { this.minimumScore = minimumScore; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<RecommendationCriterion> getCriteria() { return criteria; }

    public void replaceCriteria(List<RecommendationCriterion> newCriteria) {
        criteria.clear();
        criteria.addAll(newCriteria);
    }
}
