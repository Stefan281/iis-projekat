package com.iis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "team_analyses",
        uniqueConstraints = @UniqueConstraint(columnNames = "team_statistic_id"))
public class TeamAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_statistic_id", nullable = false)
    private TeamStatistic teamStatistic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "most_efficient_player_id")
    private OpponentPlayer mostEfficientPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "least_efficient_player_id")
    private OpponentPlayer leastEfficientPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_points_player_id")
    private OpponentPlayer topPointsPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_errors_player_id")
    private OpponentPlayer topErrorsPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_blocks_player_id")
    private OpponentPlayer topBlocksPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_serves_player_id")
    private OpponentPlayer topServesPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_assists_player_id")
    private OpponentPlayer topAssistsPlayer;

    @Column(nullable = false)
    private Integer teamEfficiency = 0;

    @Column(nullable = false)
    private Integer attackIndex = 0;

    @Column(nullable = false)
    private Integer serveIndex = 0;

    @Column(nullable = false)
    private Integer blockIndex = 0;

    @Column(nullable = false)
    private Integer disciplineIndex = 0;

    public Long getId() {
        return id;
    }

    public TeamStatistic getTeamStatistic() {
        return teamStatistic;
    }

    public void setTeamStatistic(TeamStatistic teamStatistic) {
        this.teamStatistic = teamStatistic;
    }

    public OpponentPlayer getMostEfficientPlayer() {
        return mostEfficientPlayer;
    }

    public void setMostEfficientPlayer(OpponentPlayer mostEfficientPlayer) {
        this.mostEfficientPlayer = mostEfficientPlayer;
    }

    public OpponentPlayer getLeastEfficientPlayer() {
        return leastEfficientPlayer;
    }

    public void setLeastEfficientPlayer(OpponentPlayer leastEfficientPlayer) {
        this.leastEfficientPlayer = leastEfficientPlayer;
    }

    public OpponentPlayer getTopPointsPlayer() {
        return topPointsPlayer;
    }

    public void setTopPointsPlayer(OpponentPlayer topPointsPlayer) {
        this.topPointsPlayer = topPointsPlayer;
    }

    public OpponentPlayer getTopErrorsPlayer() {
        return topErrorsPlayer;
    }

    public void setTopErrorsPlayer(OpponentPlayer topErrorsPlayer) {
        this.topErrorsPlayer = topErrorsPlayer;
    }

    public OpponentPlayer getTopBlocksPlayer() {
        return topBlocksPlayer;
    }

    public void setTopBlocksPlayer(OpponentPlayer topBlocksPlayer) {
        this.topBlocksPlayer = topBlocksPlayer;
    }

    public OpponentPlayer getTopServesPlayer() {
        return topServesPlayer;
    }

    public void setTopServesPlayer(OpponentPlayer topServesPlayer) {
        this.topServesPlayer = topServesPlayer;
    }

    public OpponentPlayer getTopAssistsPlayer() {
        return topAssistsPlayer;
    }

    public void setTopAssistsPlayer(OpponentPlayer topAssistsPlayer) {
        this.topAssistsPlayer = topAssistsPlayer;
    }

    public Integer getTeamEfficiency() {
        return teamEfficiency;
    }

    public void setTeamEfficiency(Integer teamEfficiency) {
        this.teamEfficiency = teamEfficiency;
    }

    public Integer getAttackIndex() {
        return attackIndex;
    }

    public void setAttackIndex(Integer attackIndex) {
        this.attackIndex = attackIndex;
    }

    public Integer getServeIndex() {
        return serveIndex;
    }

    public void setServeIndex(Integer serveIndex) {
        this.serveIndex = serveIndex;
    }

    public Integer getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(Integer blockIndex) {
        this.blockIndex = blockIndex;
    }

    public Integer getDisciplineIndex() {
        return disciplineIndex;
    }

    public void setDisciplineIndex(Integer disciplineIndex) {
        this.disciplineIndex = disciplineIndex;
    }
}
