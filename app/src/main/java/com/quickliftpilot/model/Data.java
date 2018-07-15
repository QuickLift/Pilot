package com.quickliftpilot.model;


/**
 * Created by adarsh on 5/1/18.
 */

public class Data {
    double st_lat,st_lng,en_lat,en_lng,d_lat,d_lng;
    String customer_id,source,destination,price,otp,seat,offer="0",paymode="Cash",cancel_charge="0",veh_type,parking_price="0";
    String waitcharge="0",triptime="0",timecharge="0",waittime="0",version="5";
    Integer accept=0;

    public Data() {
    }

    public String getOtp() {
        return otp;
    }

    public String getOffer() {
        return offer;
    }

    public String getWaitcharge() {
        return waitcharge;
    }

    public String getTriptime() {
        return triptime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWaittime() {
        return waittime;
    }

    public void setWaittime(String waittime) {
        this.waittime = waittime;
    }

    public String getTimecharge() {
        return timecharge;
    }

    public void setTimecharge(String timecharge) {
        this.timecharge = timecharge;
    }

    public void setTriptime(String triptime) {
        this.triptime = triptime;
    }

    public void setWaitcharge(String waitcharge) {
        this.waitcharge = waitcharge;
    }

    public String getParking_price() {
        return parking_price;
    }

    public void setParking_price(String parking_price) {
        this.parking_price = parking_price;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getCancel_charge() {
        return cancel_charge;
    }

    public void setCancel_charge(String cancel_charge) {
        this.cancel_charge = cancel_charge;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getSource() {
        return source;
    }

    public String getVeh_type() {
        return veh_type;
    }

    public void setVeh_type(String veh_type) {
        this.veh_type = veh_type;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getAccept() {
        return accept;
    }

    public void setAccept(Integer accept) {
        this.accept = accept;
    }

    public double getSt_lat() {
        return st_lat;
    }

    public double getD_lat() {
        return d_lat;
    }

    public void setD_lat(double d_lat) {
        this.d_lat = d_lat;
    }

    public double getD_lng() {
        return d_lng;
    }

    public void setD_lng(double d_lng) {
        this.d_lng = d_lng;
    }

    public void setSt_lat(double st_lat) {
        this.st_lat = st_lat;
    }

    public double getSt_lng() {
        return st_lng;
    }

    public void setSt_lng(double st_lng) {
        this.st_lng = st_lng;
    }

    public double getEn_lat() {
        return en_lat;
    }

    public void setEn_lat(double en_lat) {
        this.en_lat = en_lat;
    }

    public double getEn_lng() {
        return en_lng;
    }

    public void setEn_lng(double en_lng) {
        this.en_lng = en_lng;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }
}
