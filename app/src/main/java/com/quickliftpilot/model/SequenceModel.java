package com.quickliftpilot.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pandey on 1/4/18.
 */

public class SequenceModel {

    private LatLng latLng;
    private Double lat;
    private Double lng;
    private String name;
    private String type;
    private String id;
    private String address;
    private String otp;
    private String phone;
    private int seat=0;

    public String getOtp() {
        return otp;
    }

    public int getSeat() {
        return seat;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
