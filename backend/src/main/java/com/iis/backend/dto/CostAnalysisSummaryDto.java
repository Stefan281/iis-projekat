package com.iis.backend.dto;

public class CostAnalysisSummaryDto {

    private double totalSpent;
    private int tripCount;
    private double averageCostPerTrip;
    private int year;

    public CostAnalysisSummaryDto() {
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getTripCount() {
        return tripCount;
    }

    public void setTripCount(int tripCount) {
        this.tripCount = tripCount;
    }

    public double getAverageCostPerTrip() {
        return averageCostPerTrip;
    }

    public void setAverageCostPerTrip(double averageCostPerTrip) {
        this.averageCostPerTrip = averageCostPerTrip;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
