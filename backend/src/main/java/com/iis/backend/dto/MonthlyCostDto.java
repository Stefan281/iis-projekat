package com.iis.backend.dto;

public class MonthlyCostDto {

    private String monthLabel;
    private double totalCost;
    private int tripCount;

    public MonthlyCostDto() {
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getTripCount() {
        return tripCount;
    }

    public void setTripCount(int tripCount) {
        this.tripCount = tripCount;
    }
}
