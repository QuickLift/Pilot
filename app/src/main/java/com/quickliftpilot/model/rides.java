package com.quickliftpilot.model;

/**
 * Created by pandey on 28/2/18.
 */


public class rides {
    private String did;
    private String source;
    private String destination;
    private String fare;
    private String timestamp;
    private String rated;
    private String offer;
    private String cancel_charge;
    private String paymode;

    public rides() {
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

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRated() {
        return rated;
    }

    public void setRated(String rated) {
        this.rated = rated;
    }
}
