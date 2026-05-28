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
@Table(name = "behavior_comparisons")
public class BehaviorComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_player_id", nullable = false)
    private Player firstPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_player_id", nullable = false)
    private Player secondPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compared_by_user_id", nullable = false)
    private User comparedBy;

    @Column(name = "comparison_date", nullable = false)
    private LocalDate comparisonDate;

    @Column
    private Integer grade;

    @Column(length = 1000)
    private String note;

    public BehaviorComparison() {
    }

    public BehaviorComparison(Player firstPlayer, Player secondPlayer, User comparedBy, LocalDate comparisonDate, Integer grade, String note) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.comparedBy = comparedBy;
        this.comparisonDate = comparisonDate;
        this.grade = grade;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(Player secondPlayer) {
        this.secondPlayer = secondPlayer;
    }

    public User getComparedBy() {
        return comparedBy;
    }

    public void setComparedBy(User comparedBy) {
        this.comparedBy = comparedBy;
    }

    public LocalDate getComparisonDate() {
        return comparisonDate;
    }

    public void setComparisonDate(LocalDate comparisonDate) {
        this.comparisonDate = comparisonDate;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
