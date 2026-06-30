package com.iis.backend.dto;

public class TripCostItemDto {

    private long tripId;
    private String tripName;
    private String destination;
    private String departureDateFormatted;
    private String returnDateFormatted;
    private String accommodationName;
    private double accommodationCost;
    private String transportName;
    private double transportCost;
    private double totalCost;

    public TripCostItemDto() {
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureDateFormatted() {
        return departureDateFormatted;
    }

    public void setDepartureDateFormatted(String departureDateFormatted) {
        this.departureDateFormatted = departureDateFormatted;
    }

    public String getReturnDateFormatted() {
        return returnDateFormatted;
    }

    public void setReturnDateFormatted(String returnDateFormatted) {
        this.returnDateFormatted = returnDateFormatted;
    }

    public String getAccommodationName() {
        return accommodationName;
    }

    public void setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
    }

    public double getAccommodationCost() {
        return accommodationCost;
    }

    public void setAccommodationCost(double accommodationCost) {
        this.accommodationCost = accommodationCost;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public double getTransportCost() {
        return transportCost;
    }

    public void setTransportCost(double transportCost) {
        this.transportCost = transportCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

}
