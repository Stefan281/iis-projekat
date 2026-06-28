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
@Table(name = "recommendation_results")
public class RecommendationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private RecommendationModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecommendationStatus status;

    @Column(nullable = false, length = 4000)
    private String explanation;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public RecommendationResult() {
    }

    public RecommendationResult(RecommendationModel model, Player player, BigDecimal score, RecommendationStatus status, String explanation, LocalDateTime calculatedAt) {
        this.model = model;
        this.player = player;
        this.score = score;
        this.status = status;
        this.explanation = explanation;
        this.calculatedAt = calculatedAt;
    }

    public Long getId() { return id; }
    public RecommendationModel getModel() { return model; }
    public Player getPlayer() { return player; }
    public BigDecimal getScore() { return score; }
    public RecommendationStatus getStatus() { return status; }
    public String getExplanation() { return explanation; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}
