package com.iis.backend.dto;

import java.util.List;

public class CostAnalysisResponseDto {

    private CostAnalysisSummaryDto summary;
    private List<TripCostItemDto> trips;
    private List<MonthlyCostDto> monthlyBreakdown;

    public CostAnalysisResponseDto() {
    }

    public CostAnalysisSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(CostAnalysisSummaryDto summary) {
        this.summary = summary;
    }

    public List<TripCostItemDto> getTrips() {
        return trips;
    }

    public void setTrips(List<TripCostItemDto> trips) {
        this.trips = trips;
    }

    public List<MonthlyCostDto> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }

    public void setMonthlyBreakdown(List<MonthlyCostDto> monthlyBreakdown) {
        this.monthlyBreakdown = monthlyBreakdown;
    }
}
