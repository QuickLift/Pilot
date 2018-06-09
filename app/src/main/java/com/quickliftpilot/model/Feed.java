package com.quickliftpilot.model;

/**
 * Created by amit on 1/4/18.
 */

public class Feed {
    String rejectedRideCount="0";
    String canceledRidesCount="0";
    String bookedRideCount="0";
    String totalEarning="0",cash="0",offer="0",cancel_charge="0";
    String date="";

    public Feed() {
    }

    public String getRejectedRideCount() {
        return rejectedRideCount;
    }

    public void setRejectedRideCount(String rejectedRideCount) {
        this.rejectedRideCount = rejectedRideCount;
    }

    public String getCanceledRidesCount() {
        return canceledRidesCount;
    }

    public void setCanceledRidesCount(String canceledRidesCount) {
        this.canceledRidesCount = canceledRidesCount;
    }

    public String getBookedRideCount() {
        return bookedRideCount;
    }

    public void setBookedRideCount(String bookedRideCount) {
        this.bookedRideCount = bookedRideCount;
    }

    public String getTotalEarning() {
        return totalEarning;
    }

    public void setTotalEarning(String totalEarning) {
        this.totalEarning = totalEarning;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getCancel_charge() {
        return cancel_charge;
    }

    public void setCancel_charge(String cancel_charge) {
        this.cancel_charge = cancel_charge;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
