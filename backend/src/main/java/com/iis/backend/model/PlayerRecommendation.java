package com.iis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "player_recommendations")
public class PlayerRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private PlayerAnalysis analysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_by_user_id", nullable = false)
    private User recommendedBy;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(length = 1000)
    private String criteria;

    @Column(nullable = false, length = 2000)
    private String explanation;

    public PlayerRecommendation() {
    }

    public PlayerRecommendation(Player player, PlayerAnalysis analysis, User recommendedBy, LocalDate recommendationDate, String criteria, String explanation) {
        this.player = player;
        this.analysis = analysis;
        this.recommendedBy = recommendedBy;
        this.recommendationDate = recommendationDate;
        this.criteria = criteria;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PlayerAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(PlayerAnalysis analysis) {
        this.analysis = analysis;
    }

    public User getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(User recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public LocalDate getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(LocalDate recommendationDate) {
        this.recommendationDate = recommendationDate;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
