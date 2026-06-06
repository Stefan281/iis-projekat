package com.iis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "player_analyses",
        uniqueConstraints = @UniqueConstraint(columnNames = "player_statistic_id"))
public class PlayerAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_statistic_id", nullable = false)
    private PlayerStatistic playerStatistic;

    @Column(nullable = false)
    private Integer efficiency = 0;

    @Column(nullable = false)
    private Integer serveContribution = 0;

    @Column(nullable = false)
    private Integer overallRating = 0;

    public Long getId() {
        return id;
    }

    public PlayerStatistic getPlayerStatistic() {
        return playerStatistic;
    }

    public void setPlayerStatistic(PlayerStatistic playerStatistic) {
        this.playerStatistic = playerStatistic;
    }

    public Integer getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(Integer efficiency) {
        this.efficiency = efficiency;
    }

    public Integer getServeContribution() {
        return serveContribution;
    }

    public void setServeContribution(Integer serveContribution) {
        this.serveContribution = serveContribution;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }
}
